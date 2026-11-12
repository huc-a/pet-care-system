package com.pet.controller;
import com.pet.common.*;
import com.pet.entity.*;
import com.pet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    private final SysUserRepository users;
    private final PasswordEncoder encoder;
    @GetMapping public Result<List<SysUser>> list() { return Result.ok(users.findAll()); }
    @PostMapping public Result<SysUser> create(@RequestBody SysUser u) {
        if (users.existsByUsername(u.getUsername())) throw new BizException("用户名已存在");
        u.setId(null); u.setPassword(encoder.encode(u.getPassword() == null ? "123456" : u.getPassword()));
        if (u.getStatus() == null) u.setStatus(1);
        return Result.ok(users.save(u));
    }
    @PutMapping("/{id}") public Result<SysUser> update(@PathVariable Long id, @RequestBody SysUser req) {
        SysUser u = users.findById(id).orElseThrow(() -> new BizException("用户不存在"));
        if (req.getRealName() != null) u.setRealName(req.getRealName());
        if (req.getPhone() != null) u.setPhone(req.getPhone());
        if (req.getEmail() != null) u.setEmail(req.getEmail());
        if (req.getArea() != null) u.setArea(req.getArea());
        if (req.getAvatarUrl() != null) u.setAvatarUrl(req.getAvatarUrl());
        if (req.getRole() != null) u.setRole(req.getRole());
        if (req.getStatus() != null) u.setStatus(req.getStatus());
        if (req.getPassword() != null && !req.getPassword().isBlank()) u.setPassword(encoder.encode(req.getPassword()));
        return Result.ok(users.save(u));
    }
    @DeleteMapping("/{id}") public Result<Void> delete(@PathVariable Long id) { users.deleteById(id); return Result.ok(); }
}
