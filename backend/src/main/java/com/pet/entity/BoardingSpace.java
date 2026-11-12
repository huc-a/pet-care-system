package com.pet.entity;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
@Data @Entity
public class BoardingSpace {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String type;
    private Integer totalCapacity;
    private Integer occupied = 0;
    private BigDecimal pricePerDay;
    @Column(length = 500) private String facilities;
    private String sizeDesc;
    @Column(length = 500) private String description;
    private String photoUrl;
    private Integer status = 1;
}
