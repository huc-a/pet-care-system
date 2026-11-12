package com.pet.entity;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
@Data @Entity
public class ServiceItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private String pricingType;
    private BigDecimal price;
    private Integer durationMin;
    @Column(length = 500) private String description;
    private String icon;
    private String coverUrl;
    private Integer salesCount = 0;
    private Integer status = 1;
}
