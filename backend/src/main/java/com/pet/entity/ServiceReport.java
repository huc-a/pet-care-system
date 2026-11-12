package com.pet.entity;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
@Data @Entity
public class ServiceReport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    private String orderNo;
    private Long sitterId;
    private String sitterName;
    private LocalDate reportDate;
    private Boolean fed = false;
    private Boolean waterChanged = false;
    private Boolean litterCleaned = false;
    private Boolean played = false;
    private Boolean bathed = false;
    private Boolean walked = false;
    private String appetite;
    private String spirit;
    private String excretion;
    private BigDecimal temperature;
    @Column(length = 2000) private String photoUrl;
    @Column(length = 1000) private String content;
    private LocalDateTime createTime = LocalDateTime.now();
}
