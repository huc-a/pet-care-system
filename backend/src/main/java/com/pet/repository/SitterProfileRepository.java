package com.pet.repository;
import com.pet.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import java.time.*;
public interface SitterProfileRepository extends JpaRepository<SitterProfile, Long> { Optional<SitterProfile> findByUserId(Long userId); List<SitterProfile> findByAvailableTrue(); }
