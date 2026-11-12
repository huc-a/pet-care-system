package com.pet.entity;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
@Data @Entity
public class Coupon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String type;
    private BigDecimal threshold = BigDecimal.ZERO;
    private BigDecimal offAmount = BigDecimal.ZERO;
    private BigDecimal discountRate;
    private Integer totalCount = 0;
    private Integer claimedCount = 0;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Integer status = 1;
}
