package com.cupom.projeto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cupom.projeto.domain.CouponDomain;
import com.cupom.projeto.dto.CouponResponseDTO;
import com.cupom.projeto.exception.BusinessException;
import com.cupom.projeto.models.Coupon;
import com.cupom.projeto.repositories.CouponRepository;
import com.cupom.projeto.services.CouponService;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponDomain couponDomain;

    @InjectMocks
    private CouponService couponService;

    private Coupon coupon;

    @BeforeEach
    void setUp() {
        coupon = new Coupon();
        coupon.setId(UUID.randomUUID());
        coupon.setCode("ABC123");
        coupon.setDescription("Cupom teste");
        coupon.setDiscountValue(0.8);
        coupon.setExpirationDate(LocalDate.now().plusDays(10));
        coupon.setPublished(false);
        coupon.setRedeemed(false);
        coupon.setDeleted(false);
        coupon.setStatus("ACTIVE");
    }

    @Test
    void shouldFindCouponByIdSuccessfully() {
        when(couponRepository.findById(coupon.getId())).thenReturn(Optional.of(coupon));

        CouponResponseDTO result = couponService.findById(coupon.getId());

        assertNotNull(result);
        assertEquals("ABC123", result.getCode());
        verify(couponRepository).findById(coupon.getId());
    }

    @Test
    void shouldThrowExceptionWhenCouponNotFound() {
        UUID id = UUID.randomUUID();

        when(couponRepository.findById(id)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            couponService.findById(id);
        });

        assertEquals("Coupon not found", exception.getMessage());
        verify(couponRepository).findById(id);
    }

    @Test
    void shouldDeleteCouponSuccessfully() {
        when(couponRepository.findActiveById(coupon.getId())).thenReturn(Optional.of(coupon));
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);

        couponService.delete(coupon.getId());

        verify(couponRepository).findActiveById(coupon.getId());
        verify(couponRepository).save(coupon);
    }
}