package com.smartbook.dto;

import com.smartbook.entity.SubscriptionPlan;
import com.smartbook.entity.BillingCycle;
import com.smartbook.entity.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubscriptionResponse(
        Long id,
        SubscriptionPlan plan,
        BillingCycle billingCycle,
        SubscriptionStatus status,
        BigDecimal amount,
        String currency,
        String paymentProvider,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime expiresAt
) {}