package com.smartbook.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LicenseWebConfig implements WebMvcConfigurer {

    private final LicenseAccessInterceptor licenseAccessInterceptor;

    public LicenseWebConfig(
            LicenseAccessInterceptor licenseAccessInterceptor
    ) {
        this.licenseAccessInterceptor = licenseAccessInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(licenseAccessInterceptor);
    }
}