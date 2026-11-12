package com.pet.repository;
import com.pet.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import java.time.*;
public interface ReviewRepository extends JpaRepository<Review, Long> { List<Review> findBySitterIdOrderByCreateTimeDesc(Long s); List<Review> findByOwnerIdOrderByCreateTimeDesc(Long o); List<Review> findAllByOrderByCreateTimeDesc(); boolean existsByOrderId(Long o); }
