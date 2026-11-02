package com.smartbook.controller;

import com.smartbook.dto.BusinessBreakRequest;
import com.smartbook.dto.BusinessBreakResponse;
import com.smartbook.service.BusinessBreakManager;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/business-breaks")
public class BusinessBreakController {

    private final BusinessBreakManager businessBreakManager;

    public BusinessBreakController(BusinessBreakManager businessBreakManager) {
        this.businessBreakManager = businessBreakManager;
    }

    @GetMapping
    public ResponseEntity<List<BusinessBreakResponse>> getMyBreaks(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                businessBreakManager.getMyBreaks(authentication.getName())
        );
    }

    @PostMapping
    public ResponseEntity<BusinessBreakResponse> addBreak(
            Authentication authentication,
            @Valid @RequestBody BusinessBreakRequest request
    ) {
        return ResponseEntity.ok(
                businessBreakManager.addBreak(
                        authentication.getName(),
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBreak(
            Authentication authentication,
            @PathVariable Long id
    ) {
        businessBreakManager.deleteBreak(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}