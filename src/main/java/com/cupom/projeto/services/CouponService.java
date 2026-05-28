package com.cupom.projeto.services;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cupom.projeto.domain.CouponDomain;
import com.cupom.projeto.dto.CouponCreateRequestDTO;
import com.cupom.projeto.dto.CouponResponseDTO;
import com.cupom.projeto.exception.BusinessException;
import com.cupom.projeto.models.Coupon;
import com.cupom.projeto.repositories.CouponRepository;

@Service
public class CouponService {

    @Autowired
    private CouponRepository cupomRepository;

    @Autowired
    private CouponDomain couponDomain;

    public CouponService(CouponRepository cupomRepository, CouponDomain couponDomain) {
        this.cupomRepository = cupomRepository;
        this.couponDomain = couponDomain;
    }

    public CouponResponseDTO createCupom(CouponCreateRequestDTO dto) {
        Coupon coupon = couponDomain.create(
                dto.getCode(),
                dto.getDescription(),
                dto.getDiscountValue(),
                dto.getExpirationDate(),
                dto.getPublished());

        Coupon saved = cupomRepository.save(coupon);
        return toResponse(saved);
    }

    public CouponResponseDTO findById(UUID id) {
        Coupon coupon = cupomRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Coupon not found"));

        return toResponse(coupon);
    }

    public void delete(UUID id) {
        Coupon coupon = cupomRepository.findActiveById(id)
                .orElseThrow(() -> new BusinessException("Coupon not found"));

        couponDomain.delete(coupon);
        cupomRepository.save(coupon);
    }

    private CouponResponseDTO toResponse(Coupon coupon) {
        return new CouponResponseDTO(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDescription(),
                coupon.getDiscountValue(),
                coupon.getExpirationDate(),
                coupon.getStatus(),
                coupon.isPublished(),
                coupon.isRedeemed());
    }
}