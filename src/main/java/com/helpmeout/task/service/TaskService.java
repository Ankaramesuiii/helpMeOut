package com.helpmeout.task.service;

import com.helpmeout.category.entity.Category;
import com.helpmeout.category.repository.CategoryRepository;
import com.helpmeout.common.exception.BadRequestException;
import com.helpmeout.common.exception.NotFoundException;
import com.helpmeout.common.util.SecurityUtils;
import com.helpmeout.task.dto.*;
import com.helpmeout.task.entity.*;
import com.helpmeout.task.repository.TaskPhotoRepository;
import com.helpmeout.task.repository.TaskRepository;
import com.helpmeout.task.repository.TaskStatusHistoryRepository;
import com.helpmeout.user.entity.User;
import com.helpmeout.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskPhotoRepository taskPhotoRepository;
    private final TaskStatusHistoryRepository taskStatusHistoryRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        validateSchedule(request.getScheduledType(), request.getScheduledAt());

        Long currentUserId = SecurityUtils.getCurrentUserId();

        User customer = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        Task task = new Task();
        task.setCustomer(customer);
        task.setCategory(category);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setBudgetTnd(request.getBudgetTnd());
        task.setCity(request.getCity());
        task.setArea(request.getArea());
        task.setLatitude(request.getLatitude());
        task.setLongitude(request.getLongitude());
        task.setExactLocationVisible(false);
        task.setScheduledType(request.getScheduledType());
        task.setScheduledAt(request.getScheduledAt());
        task.setCategorySpecificData(request.getCategorySpecificData());
        task.setStatus(TaskStatus.OPEN);

        Task savedTask = taskRepository.save(task);

        savePhotos(savedTask, request.getPhotos());
        saveStatusHistory(savedTask, null, TaskStatus.OPEN, customer, "Task created");

        return mapToTaskResponse(savedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskListItemResponse> getMyTasks() {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        return taskRepository.findByCustomerIdOrderByCreatedAtDesc(currentUserId)
                .stream()
                .map(this::mapToTaskListItemResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        Long currentUserId = SecurityUtils.getCurrentUserId();

        boolean isOwner = task.getCustomer().getId().equals(currentUserId);

        if (!isOwner && !Boolean.TRUE.equals(task.getExactLocationVisible())) {
            return mapToTaskResponseWithoutExactLocation(task);
        }

        return mapToTaskResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskListItemResponse> browseOpenTasks(Long categoryId, String city) {
        List<TaskStatus> visibleStatuses = List.of(TaskStatus.OPEN, TaskStatus.HAS_APPLICATIONS);

        if (categoryId != null && city != null && !city.isBlank()) {
            return taskRepository.findByCategoryIdAndCityAndStatusInOrderByCreatedAtDesc(categoryId, city, visibleStatuses)
                    .stream()
                    .map(this::mapToTaskListItemResponse)
                    .toList();
        }

        return taskRepository.findByStatusInOrderByCreatedAtDesc(visibleStatuses)
                .stream()
                .map(this::mapToTaskListItemResponse)
                .toList();
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request) {
        validateSchedule(request.getScheduledType(), request.getScheduledAt());

        Long currentUserId = SecurityUtils.getCurrentUserId();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        validateTaskOwnership(task, currentUserId);
        validateTaskEditable(task);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        task.setCategory(category);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setBudgetTnd(request.getBudgetTnd());
        task.setCity(request.getCity());
        task.setArea(request.getArea());
        task.setLatitude(request.getLatitude());
        task.setLongitude(request.getLongitude());
        task.setScheduledType(request.getScheduledType());
        task.setScheduledAt(request.getScheduledAt());
        task.setCategorySpecificData(request.getCategorySpecificData());

        taskRepository.save(task);

        taskPhotoRepository.deleteByTaskId(task.getId());
        savePhotos(task, request.getPhotos());

        return mapToTaskResponse(task);
    }

    @Transactional
    public void cancelTask(Long taskId, CancelTaskRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        validateTaskOwnership(task, currentUserId);

        if (task.getStatus() == TaskStatus.IN_PROGRESS ||
                task.getStatus() == TaskStatus.COMPLETION_PENDING ||
                task.getStatus() == TaskStatus.COMPLETED ||
                task.getStatus() == TaskStatus.PAID) {
            throw new BadRequestException("Task cannot be cancelled at this stage");
        }

        TaskStatus oldStatus = task.getStatus();
        task.setStatus(TaskStatus.CANCELLED);
        taskRepository.save(task);

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        saveStatusHistory(task, oldStatus, TaskStatus.CANCELLED, user, request.getReason());
    }

    private void validateSchedule(ScheduledType scheduledType, java.time.OffsetDateTime scheduledAt) {
        if (scheduledType == ScheduledType.SCHEDULED && scheduledAt == null) {
            throw new BadRequestException("scheduledAt is required when scheduledType is SCHEDULED");
        }

        if (scheduledType == ScheduledType.ASAP) {
            if (scheduledAt != null) {
                throw new BadRequestException("scheduledAt must be null when scheduledType is ASAP");
            }
        }
    }

    private void validateTaskOwnership(Task task, Long userId) {
        if (!task.getCustomer().getId().equals(userId)) {
            throw new BadRequestException("You are not allowed to modify this task");
        }
    }

    private void validateTaskEditable(Task task) {
        if (task.getStatus() != TaskStatus.OPEN && task.getStatus() != TaskStatus.HAS_APPLICATIONS) {
            throw new BadRequestException("Task cannot be updated at this stage");
        }
    }

    private void savePhotos(Task task, List<TaskPhotoRequest> photos) {
        if (photos == null || photos.isEmpty()) {
            return;
        }

        for (TaskPhotoRequest photoRequest : photos) {
            TaskPhoto photo = new TaskPhoto();
            photo.setTask(task);
            photo.setImageUrl(photoRequest.getImageUrl());
            photo.setIsPrimary(Boolean.TRUE.equals(photoRequest.getIsPrimary()));
            taskPhotoRepository.save(photo);
        }
    }

    private void saveStatusHistory(Task task, TaskStatus oldStatus, TaskStatus newStatus, User changedBy, String reason) {
        TaskStatusHistory history = new TaskStatusHistory();
        history.setTask(task);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedByUser(changedBy);
        history.setReason(reason);
        taskStatusHistoryRepository.save(history);
    }

    private TaskResponse mapToTaskResponse(Task task) {
        List<TaskPhotoResponse> photos = taskPhotoRepository.findByTaskId(task.getId())
                .stream()
                .map(photo -> new TaskPhotoResponse(photo.getId(), photo.getImageUrl(), photo.getIsPrimary()))
                .toList();

        return TaskResponse.builder()
                .id(task.getId())
                .customerId(task.getCustomer().getId())
                .categoryId(task.getCategory().getId())
                .categoryNameAr(task.getCategory().getNameAr())
                .title(task.getTitle())
                .description(task.getDescription())
                .budgetTnd(task.getBudgetTnd())
                .city(task.getCity())
                .area(task.getArea())
                .latitude(task.getLatitude())
                .longitude(task.getLongitude())
                .exactLocationVisible(task.getExactLocationVisible())
                .categorySpecificData(task.getCategorySpecificData())
                .scheduledType(task.getScheduledType().name())
                .scheduledAt(task.getScheduledAt())
                .status(task.getStatus().name())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .photos(photos)
                .build();
    }

    private TaskResponse mapToTaskResponseWithoutExactLocation(Task task) {
        List<TaskPhotoResponse> photos = taskPhotoRepository.findByTaskId(task.getId())
                .stream()
                .map(photo -> new TaskPhotoResponse(photo.getId(), photo.getImageUrl(), photo.getIsPrimary()))
                .toList();

        return TaskResponse.builder()
                .id(task.getId())
                .customerId(task.getCustomer().getId())
                .categoryId(task.getCategory().getId())
                .categoryNameAr(task.getCategory().getNameAr())
                .title(task.getTitle())
                .description(task.getDescription())
                .budgetTnd(task.getBudgetTnd())
                .city(task.getCity())
                .area(task.getArea())
                .latitude(null)
                .longitude(null)
                .exactLocationVisible(false)
                .categorySpecificData(task.getCategorySpecificData())
                .scheduledType(task.getScheduledType().name())
                .scheduledAt(task.getScheduledAt())
                .status(task.getStatus().name())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .photos(photos)
                .build();
    }

    private TaskListItemResponse mapToTaskListItemResponse(Task task) {
        return TaskListItemResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .categoryNameAr(task.getCategory().getNameAr())
                .budgetTnd(task.getBudgetTnd())
                .city(task.getCity())
                .area(task.getArea())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .scheduledType(task.getScheduledType().name())
                .scheduledAt(task.getScheduledAt())
                .createdAt(task.getCreatedAt())
                .build();
    }
}
