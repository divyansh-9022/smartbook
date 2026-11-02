package com.smartbook.repository;

import com.smartbook.entity.Subscription;
import com.smartbook.entity.BusinessOwner;
import com.smartbook.entity.SubscriptionStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository
        extends JpaRepository<Subscription, Long> {

    List<Subscription> findByBusinessOwnerOrderByCreatedAtDesc(
            BusinessOwner businessOwner
    );

    Optional<Subscription> findFirstByBusinessOwnerAndStatusOrderByCreatedAtDesc(
            BusinessOwner businessOwner,
            SubscriptionStatus status
    );

    Optional<Subscription> findByPaymentReference(String paymentReference);
}