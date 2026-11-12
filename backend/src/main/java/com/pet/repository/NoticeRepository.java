package com.pet.repository;
import com.pet.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import java.time.*;
public interface NoticeRepository extends JpaRepository<Notice, Long> { List<Notice> findAllByOrderByCreateTimeDesc(); }
