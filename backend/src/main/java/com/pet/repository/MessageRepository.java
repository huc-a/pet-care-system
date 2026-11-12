package com.pet.repository;
import com.pet.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import java.time.*;
public interface MessageRepository extends JpaRepository<Message, Long> { List<Message> findByUserIdOrderByCreateTimeDesc(Long u); }
