package com.pet.entity;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
@Data @Entity
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, length = 40) private String txnNo;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private String userName;
    private BigDecimal amount;
    private String method;
    private String type = "PAY";
    private String status = "SUCCESS";
    private LocalDateTime createTime = LocalDateTime.now();
}
