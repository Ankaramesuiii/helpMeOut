package com.helpmeout.application.service;

import com.helpmeout.application.entity.ApplicationStatus;
import com.helpmeout.application.dto.ApplyToTaskRequest;
import com.helpmeout.application.dto.SelectProviderRequest;
import com.helpmeout.application.dto.TaskApplicationResponse;
import com.helpmeout.application.entity.TaskApplication;
import com.helpmeout.application.repository.TaskApplicationRepository;
import com.helpmeout.chat.service.ChatService;
import com.helpmeout.common.exception.BadRequestException;
import com.helpmeout.common.exception.NotFoundException;
import com.helpmeout.common.util.SecurityUtils;
import com.helpmeout.provider.entity.ProviderCategory;
import com.helpmeout.provider.entity.ProviderProfile;
import com.helpmeout.provider.repository.ProviderCategoryRepository;
import com.helpmeout.provider.repository.ProviderProfileRepository;
import com.helpmeout.task.entity.Task;
import com.helpmeout.task.entity.TaskStatus;
import com.helpmeout.task.entity.TaskStatusHistory;
import com.helpmeout.task.repository.TaskRepository;
import com.helpmeout.task.repository.TaskStatusHistoryRepository;
import com.helpmeout.user.entity.User;
import com.helpmeout.user.entity.UserProfile;
import com.helpmeout.user.repository.UserProfileRepository;
import com.helpmeout.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskApplicationService {

    private final TaskApplicationRepository taskApplicationRepository;
    private final TaskRepository taskRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final ProviderCategoryRepository providerCategoryRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final TaskStatusHistoryRepository taskStatusHistoryRepository;
    private final ChatService chatService;

    @Transactional
    public TaskApplicationResponse applyToTask(Long taskId, ApplyToTaskRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        ProviderProfile providerProfile = providerProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new BadRequestException("You must create a provider profile before applying"));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        if (task.getCustomer().getId().equals(currentUserId)) {
            throw new BadRequestException("You cannot apply to your own task");
        }

        if (task.getStatus() != TaskStatus.OPEN && task.getStatus() != TaskStatus.HAS_APPLICATIONS) {
            throw new BadRequestException("This task is not open for applications");
        }

        if (taskApplicationRepository.existsByTaskIdAndProviderProfileId(taskId, providerProfile.getId())) {
            throw new BadRequestException("You have already applied to this task");
        }

        TaskApplication application = new TaskApplication();
        application.setTask(task);
        application.setProviderProfile(providerProfile);
        application.setProposedPrice(request.getProposedPrice());
        application.setApplicationMessage(request.getApplicationMessage());
        application.setStatus(ApplicationStatus.PENDING);

        TaskApplication savedApplication = taskApplicationRepository.save(application);
        chatService.createChatForApplication(savedApplication);

        if (task.getStatus() == TaskStatus.OPEN) {
            TaskStatus oldStatus = task.getStatus();
            task.setStatus(TaskStatus.HAS_APPLICATIONS);
            taskRepository.save(task);

            User changedBy = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new NotFoundException("User not found"));

            saveTaskStatusHistory(task, oldStatus, TaskStatus.HAS_APPLICATIONS, changedBy, "First application received");
        }

        return mapToResponse(savedApplication);
    }

    @Transactional(readOnly = true)
    public List<TaskApplicationResponse> getApplicationsForMyTask(Long taskId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        if (!task.getCustomer().getId().equals(currentUserId)) {
            throw new BadRequestException("You are not allowed to view applications for this task");
        }

        return taskApplicationRepository.findByTaskIdOrderByCreatedAtDesc(taskId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskApplicationResponse> getMyApplications() {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        ProviderProfile providerProfile = providerProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new BadRequestException("Provider profile not found"));

        return taskApplicationRepository.findByProviderProfileIdOrderByCreatedAtDesc(providerProfile.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public TaskApplicationResponse selectProviderForTask(Long taskId, SelectProviderRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        if (!task.getCustomer().getId().equals(currentUserId)) {
            throw new BadRequestException("You are not allowed to select provider for this task");
        }

        if (task.getStatus() != TaskStatus.OPEN && task.getStatus() != TaskStatus.HAS_APPLICATIONS) {
            throw new BadRequestException("Provider cannot be selected at this stage");
        }

        TaskApplication selectedApplication = taskApplicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new NotFoundException("Application not found"));

        if (!selectedApplication.getTask().getId().equals(taskId)) {
            throw new BadRequestException("Application does not belong to this task");
        }

        selectedApplication.setStatus(ApplicationStatus.SELECTED);
        taskApplicationRepository.save(selectedApplication);

        List<TaskApplication> pendingApplications =
                taskApplicationRepository.findByTaskIdAndStatus(taskId, ApplicationStatus.PENDING);

        for (TaskApplication application : pendingApplications) {
            if (!application.getId().equals(selectedApplication.getId())) {
                application.setStatus(ApplicationStatus.REJECTED);
                taskApplicationRepository.save(application);
            }
        }

        TaskStatus oldStatus = task.getStatus();
        task.setSelectedProvider(selectedApplication.getProviderProfile());
        task.setExactLocationVisible(true);
        task.setStatus(TaskStatus.PROVIDER_SELECTED);
        taskRepository.save(task);

        User changedBy = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        saveTaskStatusHistory(task, oldStatus, TaskStatus.PROVIDER_SELECTED, changedBy, "Provider selected");

        chatService.closeNonSelectedChatsForTask(taskId, selectedApplication.getId());

        return mapToResponse(selectedApplication);
    }

    @Transactional
    public void withdrawMyApplication(Long applicationId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        ProviderProfile providerProfile = providerProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new BadRequestException("Provider profile not found"));

        TaskApplication application = taskApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        if (!application.getProviderProfile().getId().equals(providerProfile.getId())) {
            throw new BadRequestException("You are not allowed to withdraw this application");
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BadRequestException("Only pending applications can be withdrawn");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        taskApplicationRepository.save(application);
    }

    private void saveTaskStatusHistory(Task task,
                                       TaskStatus oldStatus,
                                       TaskStatus newStatus,
                                       User changedBy,
                                       String reason) {
        TaskStatusHistory history = new TaskStatusHistory();
        history.setTask(task);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedByUser(changedBy);
        history.setReason(reason);
        taskStatusHistoryRepository.save(history);
    }

    private TaskApplicationResponse mapToResponse(TaskApplication application) {
        ProviderProfile providerProfile = application.getProviderProfile();
        User providerUser = providerProfile.getUser();

        UserProfile providerUserProfile = userProfileRepository.findByUserId(providerUser.getId())
                .orElseThrow(() -> new NotFoundException("Provider user profile not found"));

        List<String> categories = providerCategoryRepository.findByProviderProfileId(providerProfile.getId())
                .stream()
                .map(ProviderCategory::getCategory)
                .map(category -> category.getNameAr())
                .toList();

        return TaskApplicationResponse.builder()
                .applicationId(application.getId())
                .taskId(application.getTask().getId())
                .taskTitle(application.getTask().getTitle())
                .status(application.getStatus().name())
                .proposedPrice(application.getProposedPrice())
                .applicationMessage(application.getApplicationMessage())
                .createdAt(application.getCreatedAt())
                .providerId(providerProfile.getId())
                .providerUserId(providerUser.getId())
                .providerFullName(providerUserProfile.getFullName())
                .providerPhotoUrl(providerUserProfile.getProfilePhotoUrl())
                .providerRating(providerProfile.getProviderRating())
                .completedTasksCount(providerProfile.getCompletedTasksCount())
                .providerCategories(categories)
                .providerCity(providerUserProfile.getCity())
                .providerArea(providerUserProfile.getArea())
                .build();
    }
}