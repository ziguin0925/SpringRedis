package org.example.couponcore.repository.mysql;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.example.couponcore.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {

    // 쓰기 lcok(x-lock)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Coupon c where c.id = :id")
    Optional<Coupon> findCouponWithLock(long id);
}
