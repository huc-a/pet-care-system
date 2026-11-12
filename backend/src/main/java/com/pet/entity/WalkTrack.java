package com.pet.entity;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
@Data @Entity
public class WalkTrack {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    private String orderNo;
    private Long sitterId;
    private String petName;
    @Column(length = 4000) private String points;
    private BigDecimal distanceKm;
    private Integer durationMin;
    private BigDecimal paceMinPerKm;
    private LocalDate walkDate;
    private LocalDateTime createTime = LocalDateTime.now();
}
