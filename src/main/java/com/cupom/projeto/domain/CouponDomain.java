package com.cupom.projeto.domain;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cupom.projeto.exception.BusinessException;
import com.cupom.projeto.models.Coupon;

@Component
public class CouponDomain {

    public Coupon create(
            String code,
            String description,
            Double discountValue,
            LocalDate expirationDate,
            Boolean published) {

        validateCode(code);
        validateDescription(description);
        validateDiscountValue(discountValue);
        validateExpirationDate(expirationDate);

        String sanitizedCode = sanitizeCode(code);
        validateSanitizedCodeLength(sanitizedCode);

        Coupon coupon = new Coupon();
        coupon.setId(UUID.randomUUID());
        coupon.setCode(sanitizedCode);
        coupon.setDescription(description.trim());
        coupon.setDiscountValue(discountValue);
        coupon.setExpirationDate(expirationDate);
        coupon.setPublished(Boolean.TRUE.equals(published));
        coupon.setRedeemed(false);
        coupon.setDeleted(false);
        coupon.setStatus("ACTIVE");

        return coupon;
    }

    public void delete(Coupon coupon) {
        if (coupon == null) {
            throw new BusinessException("Coupon not found");
        }

        if (coupon.isDeleted()) {
            throw new BusinessException("Coupon already deleted");
        }

        coupon.setDeleted(true);
        coupon.setStatus("INACTIVE");
    }

    private void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException("Code is required");
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new BusinessException("Description is required");
        }
    }

    private void validateDiscountValue(Double discountValue) {
        if (discountValue == null || discountValue < 0.5) {
            throw new BusinessException("Discount value must be at least 0.5");
        }
    }

    private void validateExpirationDate(LocalDate expirationDate) {
        if (expirationDate == null) {
            throw new BusinessException("Expiration date is required");
        }

        if (expirationDate.isBefore(LocalDate.now())) {
            throw new BusinessException("Expiration date cannot be in the past");
        }
    }

    private String sanitizeCode(String code) {
        return code.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
    }

    private void validateSanitizedCodeLength(String sanitizedCode) {
        if (sanitizedCode.length() != 6) {
            throw new BusinessException(
                    "Code must contain exactly 6 alphanumeric characters after sanitization");
        }
    }
}