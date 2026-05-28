package com.cupom.projeto.models;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Cupom")
@Entity
public class Coupon {

    @Id
    private UUID id;

    private Double discountValue;
    private String code;
    private String description;
    private LocalDate expirationDate;
    private String status;
    private boolean published;
    private boolean redeemed;
    private boolean deleted;

}
