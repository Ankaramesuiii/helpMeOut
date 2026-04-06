package com.helpmeout.workflow.service;

import com.helpmeout.common.exception.BadRequestException;
import com.helpmeout.common.exception.NotFoundException;
import com.helpmeout.common.util.SecurityUtils;
import com.helpmeout.payment.entity.PaymentConfirmation;
import com.helpmeout.payment.repository.PaymentConfirmationRepository;
import com.helpmeout.task.entity.Task;
import com.helpmeout.task.entity.TaskStatus;
import com.helpmeout.task.entity.TaskStatusHistory;
import com.helpmeout.task.repository.TaskRepository;
import com.helpmeout.task.repository.TaskStatusHistoryRepository;
import com.helpmeout.user.entity.User;
import com.helpmeout.user.repository.UserRepository;
import com.helpmeout.workflow.dto.WorkflowActionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class TaskWorkflowService {

    private final TaskRepository taskRepository;
    private final TaskStatusHistoryRepository taskStatusHistoryRepository;
    private final PaymentConfirmationRepository paymentConfirmationRepository;
    private final UserRepository userRepository;

    @Transactional
    public WorkflowActionResponse startTask(Long taskId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Task task = getTask(taskId);

        validateSelectedProvider(task, currentUserId);

        if (task.getStatus() != TaskStatus.PROVIDER_SELECTED) {
            throw new BadRequestException("Task cannot be started at this stage");
        }

        TaskStatus oldStatus = task.getStatus();
        task.setStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(task);

        saveStatusHistory(task, oldStatus, TaskStatus.IN_PROGRESS, currentUserId, "Provider started task");

        return WorkflowActionResponse.builder()
                .taskId(task.getId())
                .status(task.getStatus().name())
                .message("Task started successfully")
                .build();
    }

    @Transactional
    public WorkflowActionResponse completeTask(Long taskId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Task task = getTask(taskId);

        validateSelectedProvider(task, currentUserId);

        if (task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new BadRequestException("Task cannot be marked completed at this stage");
        }

        TaskStatus oldStatus = task.getStatus();
        task.setStatus(TaskStatus.COMPLETION_PENDING);
        taskRepository.save(task);

        saveStatusHistory(task, oldStatus, TaskStatus.COMPLETION_PENDING, currentUserId, "Provider marked task completed");

        return WorkflowActionResponse.builder()
                .taskId(task.getId())
                .status(task.getStatus().name())
                .message("Task marked as completed, waiting for customer confirmation")
                .build();
    }

    @Transactional
    public WorkflowActionResponse confirmCompletion(Long taskId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Task task = getTask(taskId);

        validateCustomer(task, currentUserId);

        if (task.getStatus() != TaskStatus.COMPLETION_PENDING) {
            throw new BadRequestException("Task is not waiting for completion confirmation");
        }

        TaskStatus oldStatus = task.getStatus();
        task.setStatus(TaskStatus.COMPLETED);
        taskRepository.save(task);

        saveStatusHistory(task, oldStatus, TaskStatus.COMPLETED, currentUserId, "Customer confirmed completion");

        return WorkflowActionResponse.builder()
                .taskId(task.getId())
                .status(task.getStatus().name())
                .message("Task completion confirmed")
                .build();
    }

    @Transactional
    public WorkflowActionResponse markPaid(Long taskId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Task task = getTask(taskId);

        validateCustomer(task, currentUserId);

        if (task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.PAYMENT_PENDING) {
            throw new BadRequestException("Task is not ready for payment confirmation");
        }

        PaymentConfirmation payment = paymentConfirmationRepository.findByTaskId(taskId)
                .orElseGet(() -> {
                    PaymentConfirmation p = new PaymentConfirmation();
                    p.setTask(task);
                    return paymentConfirmationRepository.save(p);
                });

        payment.setCustomerPaid(true);
        payment.setCustomerPaidAt(OffsetDateTime.now());
        paymentConfirmationRepository.save(payment);

        if (Boolean.TRUE.equals(payment.getProviderReceived())) {
            TaskStatus oldStatus = task.getStatus();
            task.setStatus(TaskStatus.PAID);
            taskRepository.save(task);
            saveStatusHistory(task, oldStatus, TaskStatus.PAID, currentUserId, "Customer marked paid and provider already confirmed");
        } else {
            if (task.getStatus() != TaskStatus.PAYMENT_PENDING) {
                TaskStatus oldStatus = task.getStatus();
                task.setStatus(TaskStatus.PAYMENT_PENDING);
                taskRepository.save(task);
                saveStatusHistory(task, oldStatus, TaskStatus.PAYMENT_PENDING, currentUserId, "Customer marked payment as paid");
            }
        }

        return WorkflowActionResponse.builder()
                .taskId(task.getId())
                .status(task.getStatus().name())
                .message("Payment marked from customer side")
                .build();
    }

    @Transactional
    public WorkflowActionResponse confirmPayment(Long taskId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Task task = getTask(taskId);

        validateSelectedProvider(task, currentUserId);

        if (task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.PAYMENT_PENDING) {
            throw new BadRequestException("Task is not ready for payment confirmation");
        }

        PaymentConfirmation payment = paymentConfirmationRepository.findByTaskId(taskId)
                .orElseGet(() -> {
                    PaymentConfirmation p = new PaymentConfirmation();
                    p.setTask(task);
                    return paymentConfirmationRepository.save(p);
                });

        payment.setProviderReceived(true);
        payment.setProviderConfirmedAt(OffsetDateTime.now());
        paymentConfirmationRepository.save(payment);

        if (Boolean.TRUE.equals(payment.getCustomerPaid())) {
            TaskStatus oldStatus = task.getStatus();
            task.setStatus(TaskStatus.PAID);
            taskRepository.save(task);
            saveStatusHistory(task, oldStatus, TaskStatus.PAID, currentUserId, "Provider confirmed payment and customer already marked paid");
        } else {
            if (task.getStatus() != TaskStatus.PAYMENT_PENDING) {
                TaskStatus oldStatus = task.getStatus();
                task.setStatus(TaskStatus.PAYMENT_PENDING);
                taskRepository.save(task);
                saveStatusHistory(task, oldStatus, TaskStatus.PAYMENT_PENDING, currentUserId, "Provider confirmed payment received");
            }
        }

        return WorkflowActionResponse.builder()
                .taskId(task.getId())
                .status(task.getStatus().name())
                .message("Payment confirmed from provider side")
                .build();
    }

    private Task getTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
    }

    private void validateCustomer(Task task, Long currentUserId) {
        if (!task.getCustomer().getId().equals(currentUserId)) {
            throw new BadRequestException("You are not allowed to perform this action");
        }
    }

    private void validateSelectedProvider(Task task, Long currentUserId) {
        if (task.getSelectedProvider() == null || !task.getSelectedProvider().getUser().getId().equals(currentUserId)) {
            throw new BadRequestException("Only the selected provider can perform this action");
        }
    }

    private void saveStatusHistory(Task task, TaskStatus oldStatus, TaskStatus newStatus, Long changedByUserId, String reason) {
        User changedBy = userRepository.findById(changedByUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        TaskStatusHistory history = new TaskStatusHistory();
        history.setTask(task);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedByUser(changedBy);
        history.setReason(reason);
        taskStatusHistoryRepository.save(history);
    }
}