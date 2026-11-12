package com.pet.entity;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
@Data @Entity
public class HealthRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long petId;
    private String petName;
    private String type;
    private String name;
    private LocalDate recordDate;
    private LocalDate nextDueDate;
    private BigDecimal weight;
    @Column(length = 500) private String note;
    private LocalDateTime createTime = LocalDateTime.now();
}
