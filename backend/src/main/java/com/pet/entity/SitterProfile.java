package com.pet.entity;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
@Data @Entity
public class SitterProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String area;
    @Column(length = 500) private String specialties;
    private String petTypes;
    private Integer experienceYears;
    @Column(length = 1000) private String intro;
    private BigDecimal rating = new BigDecimal("5.0");
    private Integer orderCount = 0;
    private Boolean available = true;
    private String certStatus;
    private BigDecimal distanceKm;
}
