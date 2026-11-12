package com.pet.repository;
import com.pet.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import java.time.*;
public interface CheckinRecordRepository extends JpaRepository<CheckinRecord, Long> { List<CheckinRecord> findByOrderId(Long o); List<CheckinRecord> findByStatus(String s); Optional<CheckinRecord> findByOrderIdAndStatus(Long o, String s); }
