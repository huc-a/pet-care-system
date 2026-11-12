package com.pet.entity;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
@Data @Entity
public class BookingOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, length = 30) private String orderNo;
    private Long ownerId;
    private String ownerName;
    @Column(length = 200) private String petIds;
    @Column(length = 300) private String petNames;
    private Integer petCount = 1;
    private Long serviceId;
    private String serviceName;
    private String serviceCategory;
    private Long sitterId;
    private String sitterName;
    private Long spaceId;
    private String spaceName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer days = 1;
    private Integer timesPerDay = 1;
    private Integer quantity = 1;
    @Column(length = 500) private String addonNames;
    @Column(length = 1000) private String priceDetail;
    private BigDecimal baseAmount = BigDecimal.ZERO;
    private BigDecimal addonAmount = BigDecimal.ZERO;
    private BigDecimal holidayFactor = BigDecimal.ONE;
    private BigDecimal holidayAmount = BigDecimal.ZERO;
    private BigDecimal longStayDiscount = BigDecimal.ZERO;
    private BigDecimal multiPetDiscount = BigDecimal.ZERO;
    private BigDecimal couponDiscount = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private Long userCouponId;
    private String status = "PENDING";
    private String payStatus = "UNPAID";
    private String area;
    @Column(length = 500) private String address;
    @Column(length = 500) private String remark;
    private LocalDateTime createTime = LocalDateTime.now();
    private LocalDateTime updateTime = LocalDateTime.now();
}
