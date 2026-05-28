package com.cupom.projeto.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cupom.projeto.dto.CouponCreateRequestDTO;
import com.cupom.projeto.dto.CouponResponseDTO;
import com.cupom.projeto.services.CouponService;

@RestController
@RequestMapping("/coupon")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    public ResponseEntity<CouponResponseDTO> create(
            @RequestBody CouponCreateRequestDTO dto) {

        CouponResponseDTO response = couponService.createCupom(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        couponService.delete(id);
        return ResponseEntity.noContent().build();
    }
}