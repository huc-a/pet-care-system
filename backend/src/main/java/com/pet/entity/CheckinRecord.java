package com.pet.entity;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
@Data @Entity
public class CheckinRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    private String orderNo;
    private Long spaceId;
    private String spaceName;
    private String petNames;
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime;
    private String status = "IN";
    private String operator;
    @Column(length = 500) private String remark;
}
