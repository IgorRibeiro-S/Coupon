package com.cupom.projeto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.cupom.projeto.domain.CouponDomain;
import com.cupom.projeto.exception.BusinessException;
import com.cupom.projeto.models.Coupon;

@SpringBootTest
class CouponDomainTest {

	private CouponDomain couponDomain;

	@BeforeEach
	void setUp() {
		couponDomain = new CouponDomain();
	}

	@Test
	void shouldCreateCouponSuccessfully() {
		Coupon coupon = couponDomain.create(
				"ABC123",
				"Cupom teste",
				0.8,
				LocalDate.now().plusDays(10),
				false);

		assertEquals("ABC123", coupon.getCode());
		assertEquals("Cupom teste", coupon.getDescription());
		assertEquals(0.8, coupon.getDiscountValue());
		assertFalse(coupon.isDeleted());
		assertFalse(coupon.isRedeemed());
		assertFalse(coupon.isPublished());
	}

	@Test
	void shouldSanitizeCodeWhenCreatingCoupon() {
		Coupon coupon = couponDomain.create(
				"ABC-123",
				"Cupom teste",
				1.0,
				LocalDate.now().plusDays(5),
				false);

		assertEquals("ABC123", coupon.getCode());
	}

	@Test
	void shouldThrowExceptionWhenDiscountValueIsLessThanMinimum() {
		BusinessException exception = assertThrows(BusinessException.class, () -> {
			couponDomain.create(
					"ABC123",
					"Cupom teste",
					0.4,
					LocalDate.now().plusDays(5),
					false);
		});

		assertEquals("Discount value must be at least 0.5", exception.getMessage());
	}

	@Test
	void shouldThrowExceptionWhenExpirationDateIsInThePast() {
		BusinessException exception = assertThrows(BusinessException.class, () -> {
			couponDomain.create(
					"ABC123",
					"Cupom teste",
					1.0,
					LocalDate.now().minusDays(1),
					false);
		});

		assertEquals("Expiration date cannot be in the past", exception.getMessage());
	}

	@Test
	void shouldSoftDeleteCoupon() {
		Coupon coupon = couponDomain.create(
				"ABC123",
				"Cupom teste",
				1.0,
				LocalDate.now().plusDays(5),
				false);

		couponDomain.delete(coupon);

		assertTrue(coupon.isDeleted());
	}

	@Test
	void shouldThrowExceptionWhenDeletingAlreadyDeletedCoupon() {
		Coupon coupon = couponDomain.create(
				"ABC123",
				"Cupom teste",
				1.0,
				LocalDate.now().plusDays(5),
				false);

		couponDomain.delete(coupon);

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			couponDomain.delete(coupon);
		});

		assertEquals("Coupon already deleted", exception.getMessage());
	}

}
