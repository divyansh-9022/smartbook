package com.smartbook.config;

import com.smartbook.entity.BusinessOwner;
import com.smartbook.repository.BusinessOwnerRepository;
import com.smartbook.repository.LicenseActivationRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import org.springframework.beans.factory.annotation.Value;

@Component
public class LicenseAccessInterceptor implements HandlerInterceptor {

    private final BusinessOwnerRepository ownerRepository;
    private final LicenseActivationRepository licenseRepository;
    private final boolean enforcementEnabled;

    public LicenseAccessInterceptor(
            BusinessOwnerRepository ownerRepository,
            LicenseActivationRepository licenseRepository,
            @Value("${app.license.enforcement-enabled:false}")
            boolean enforcementEnabled
    ) {
        this.ownerRepository = ownerRepository;
        this.licenseRepository = licenseRepository;
        this.enforcementEnabled = enforcementEnabled;
    }

    @Override
    public boolean preHandle(

            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        if (!enforcementEnabled) {
            return true;
        }

        String path = request.getRequestURI();

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (path.startsWith("/api/auth/")
                || path.equals("/api/business-owners/register")
                || path.equals("/api/health")
                || path.startsWith("/api/license/")
                || path.startsWith("/api/public/businesses/")) {
            return true;
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return true;
        }

        BusinessOwner owner = ownerRepository.findByEmail(
                authentication.getName()
        ).orElse(null);

        if (owner == null) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Business account not found."
            );
            return false;
        }

        boolean activated =
                licenseRepository.findByBusinessOwner(owner).isPresent();

        if (!activated) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(
                    "{\"code\":\"LICENSE_REQUIRED\","
                            + "\"message\":\"Please activate your SmartBook licence.\"}"
            );

            return false;
        }

        return true;
    }
}