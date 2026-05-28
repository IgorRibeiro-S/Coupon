package com.cupom.projeto.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CouponResponseDTO {

    private UUID id;
    private String code;
    private String description;
    private Double discountValue;
    private LocalDate expirationDate;
    private String status;
    private boolean published;
    private boolean redeemed;
}