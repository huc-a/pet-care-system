package com.pet.repository;
import com.pet.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import java.time.*;
public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> { List<HealthRecord> findByPetIdOrderByRecordDateDesc(Long p); List<HealthRecord> findByNextDueDateBetween(LocalDate a, LocalDate b); }
