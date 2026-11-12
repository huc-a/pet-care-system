package com.pet.repository;
import com.pet.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import java.time.*;
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> { List<UserCoupon> findByUserId(Long u); Optional<UserCoupon> findByIdAndUserId(Long id, Long u); boolean existsByCouponIdAndUserId(Long c, Long u); }
