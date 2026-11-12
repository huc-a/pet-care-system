package com.pet.repository;
import com.pet.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import java.time.*;
public interface BookingOrderRepository extends JpaRepository<BookingOrder, Long> { List<BookingOrder> findByOwnerIdOrderByCreateTimeDesc(Long o); List<BookingOrder> findBySitterIdOrderByCreateTimeDesc(Long s); List<BookingOrder> findByStatusOrderByCreateTimeDesc(String s); List<BookingOrder> findAllByOrderByCreateTimeDesc(); Optional<BookingOrder> findByOrderNo(String n); long countByStatus(String s); List<BookingOrder> findByCreateTimeAfter(LocalDateTime t); }
