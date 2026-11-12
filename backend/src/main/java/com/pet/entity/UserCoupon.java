package com.pet.entity;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
@Data @Entity
public class UserCoupon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long couponId;
    private Long userId;
    private String status = "UNUSED";
    private Long orderId;
    private LocalDateTime claimTime = LocalDateTime.now();
    private LocalDateTime useTime;
}
