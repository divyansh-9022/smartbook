package com.smartbook.service;

import com.smartbook.dto.SubscriptionResponse;
import com.smartbook.entity.BusinessOwner;
import com.smartbook.entity.Subscription;
import com.smartbook.entity.SubscriptionStatus;
import com.smartbook.repository.BusinessOwnerRepository;
import com.smartbook.repository.SubscriptionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubscriptionManager {

    private final SubscriptionRepository subscriptionRepository;
    private final BusinessOwnerRepository ownerRepository;

    public SubscriptionManager(
            SubscriptionRepository subscriptionRepository,
            BusinessOwnerRepository ownerRepository
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.ownerRepository = ownerRepository;
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getMySubscriptions(String email) {

        BusinessOwner owner = ownerRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business owner not found."));

        return subscriptionRepository
                .findByBusinessOwnerOrderByCreatedAtDesc(owner)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getActiveSubscription(String email) {

        BusinessOwner owner = ownerRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business owner not found."));

        return subscriptionRepository
                .findFirstByBusinessOwnerAndStatusOrderByCreatedAtDesc(
                        owner,
                        SubscriptionStatus.ACTIVE
                )
                .map(this::toResponse)
                .orElse(null);
    }

    private SubscriptionResponse toResponse(Subscription subscription) {

        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getPlan(),
                subscription.getBillingCycle(),
                subscription.getStatus(),
                subscription.getAmount(),
                subscription.getCurrency(),
                subscription.getPaymentProvider(),
                subscription.getCreatedAt(),
                subscription.getStartedAt(),
                subscription.getExpiresAt()
        );
    }
}