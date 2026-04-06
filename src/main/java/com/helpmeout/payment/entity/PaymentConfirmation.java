package com.helpmeout.payment.entity;

import com.helpmeout.common.entity.BaseEntity;
import com.helpmeout.task.entity.Task;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "payment_confirmations")
public class PaymentConfirmation extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false, unique = true)
    private Task task;

    @Column(name = "customer_paid", nullable = false)
    private Boolean customerPaid = false;

    @Column(name = "customer_paid_at")
    private OffsetDateTime customerPaidAt;

    @Column(name = "provider_received", nullable = false)
    private Boolean providerReceived = false;

    @Column(name = "provider_confirmed_at")
    private OffsetDateTime providerConfirmedAt;
}