package com.smartbook.controller;

import com.smartbook.dto.SubscriptionResponse;
import com.smartbook.service.SubscriptionManager;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionManager subscriptionManager;

    public SubscriptionController(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionResponse>> getMySubscriptions(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                subscriptionManager.getMySubscriptions(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/active")
    public ResponseEntity<SubscriptionResponse> getActiveSubscription(
            Authentication authentication
    ) {
        SubscriptionResponse subscription =
                subscriptionManager.getActiveSubscription(
                        authentication.getName()
                );

        if (subscription == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(subscription);
    }
}