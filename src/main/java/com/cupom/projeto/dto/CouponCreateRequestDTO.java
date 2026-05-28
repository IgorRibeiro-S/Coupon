package com.cupom.projeto.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CouponCreateRequestDTO {

    @NotBlank(message = "code is required")
    private String code;

    @NotBlank(message = "description is required")
    private String description;

    @NotNull(message = "discountValue is required")
    private Double discountValue;

    @NotNull(message = "expirationDate is required")
    private LocalDate expirationDate;

    private Boolean published;
}