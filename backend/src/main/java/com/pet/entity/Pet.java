package com.pet.entity;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
@Data @Entity
public class Pet {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long ownerId;
    private String name;
    private String species;
    private String breed;
    private String gender;
    private Integer ageMonths;
    private BigDecimal weight;
    private Boolean neutered = false;
    private String color;
    @Column(length = 500) private String personality;
    @Column(length = 500) private String allergy;
    @Column(length = 500) private String feedingHabit;
    private String hospital;
    private String vaccineStatus;
    private String emoji;
    private String photoUrl;
    private LocalDateTime createTime = LocalDateTime.now();
}
