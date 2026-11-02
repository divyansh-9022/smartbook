package com.smartbook.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    // Checks a licence without consuming an activation.
    public boolean verifyLicenseKey(String licenseKey) {
        return verify(licenseKey, false);
    }

    // Counts one use for a new installation.
    public boolean activateLicenseKey(String licenseKey) {
        return verify(licenseKey, true);
    }

    private boolean verify(String licenseKey, boolean incrementUses) {

        if (licenseKey == null || licenseKey.isBlank()) {
            return false;
        }

        MultiValueMap<String, String> formData =
                new LinkedMultiValueMap<>();

        formData.add("product_id", productId);
        formData.add("license_key", licenseKey.trim());
        formData.add("increment_uses_count",
                Boolean.toString(incrementUses));

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

            if (refunded || disputed || chargebacked) {
                return false;
            }

            // A new installation is accepted only when Gumroad
            // reports that this is the first recorded use.
            if (incrementUses) {
                Object usesObject = responseData.get("uses");

                if (!(usesObject instanceof Number uses)
                        || uses.intValue() != 1) {
                    return false;
                }
            }

            return true;

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