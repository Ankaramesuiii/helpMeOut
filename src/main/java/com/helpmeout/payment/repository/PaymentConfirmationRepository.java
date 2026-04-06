package com.helpmeout.payment.repository;

import com.helpmeout.payment.entity.PaymentConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentConfirmationRepository extends JpaRepository<PaymentConfirmation, Long> {
    Optional<PaymentConfirmation> findByTaskId(Long taskId);
}