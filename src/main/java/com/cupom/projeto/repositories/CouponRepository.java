package com.cupom.projeto.repositories;

import com.cupom.projeto.models.Coupon;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    @Query("select c from Coupon c where c.id = :id and c.deleted = false")
    Optional<Coupon> findActiveById(@Param("id") UUID id);
}
