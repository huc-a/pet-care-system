package com.pet.repository;
import com.pet.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import java.time.*;
public interface PetRepository extends JpaRepository<Pet, Long> { List<Pet> findByOwnerId(Long ownerId); }
