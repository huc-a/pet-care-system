package com.pet.repository;
import com.pet.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import java.time.*;
public interface SysUserRepository extends JpaRepository<SysUser, Long> { Optional<SysUser> findByUsername(String u); boolean existsByUsername(String u); List<SysUser> findByRole(String r); }
