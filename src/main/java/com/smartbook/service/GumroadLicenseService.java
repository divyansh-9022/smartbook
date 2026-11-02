package com.smartbook.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GumroadLicenseService {

    private static final String GUMROAD_VERIFY_URL =
            "https://api.gumroad.com/v2/licenses/verify";

    private final String productId;
    private final RestTemplate restTemplate = new RestTemplate();

    public GumroadLicenseService(
            @Value("${app.gumroad.product-id}") String productId
    ) {
        this.productId = productId;
    }

    public boolean verifyLicenseKey(String licenseKey) {

        if (licenseKey == null || licenseKey.isBlank()) {
            return false;
        }

        MultiValueMap<String, String> formData =
                new LinkedMultiValueMap<>();

        formData.add("product_id", productId);
        formData.add("license_key", licenseKey.trim());
        formData.add("increment_uses_count", "false");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(formData, headers);

        try {

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            GUMROAD_VERIFY_URL,
                            request,
                            Map.class
                    );

            Map<String, Object> responseData = response.getBody();

            if (responseData == null
                    || !Boolean.TRUE.equals(responseData.get("success"))) {
                return false;
            }

            Object purchaseObject = responseData.get("purchase");

            if (!(purchaseObject instanceof Map<?, ?> purchase)) {
                return false;
            }

            String returnedProductId =
                    String.valueOf(purchase.get("product_id"));

            if (!productId.equals(returnedProductId)) {
                return false;
            }

            boolean refunded =
                    Boolean.TRUE.equals(purchase.get("refunded"));

            boolean disputed =
                    Boolean.TRUE.equals(purchase.get("disputed"));

            boolean chargebacked =
                    Boolean.TRUE.equals(purchase.get("chargebacked"));

            return !refunded && !disputed && !chargebacked;

        } catch (HttpClientErrorException.NotFound exception) {

            return false;

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to contact Gumroad licence verification service.",
                    exception
            );
        }
    }
}