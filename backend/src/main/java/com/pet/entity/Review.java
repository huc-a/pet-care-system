package com.pet.entity;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
@Data @Entity
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    private String orderNo;
    private Long ownerId;
    private String ownerName;
    private Long sitterId;
    private String sitterName;
    private Integer rating;
    @Column(length = 1000) private String content;
    private String tags;
    private Boolean hidden = false;
    private LocalDateTime createTime = LocalDateTime.now();
}
