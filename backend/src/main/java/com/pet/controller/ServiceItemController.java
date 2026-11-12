package com.pet.controller;
import com.pet.common.*;
import com.pet.entity.*;
import com.pet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceItemController {
    private final ServiceItemRepository repo;
    @GetMapping public Result<List<ServiceItem>> list() { return Result.ok(repo.findAll()); }
    @GetMapping("/{id}") public Result<ServiceItem> get(@PathVariable Long id) {
        return Result.ok(repo.findById(id).orElseThrow(() -> new BizException("服务不存在"))); }
    @PostMapping @PreAuthorize("hasRole('ADMIN')")
    public Result<ServiceItem> create(@RequestBody ServiceItem s) { s.setId(null); return Result.ok(repo.save(s)); }
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public Result<ServiceItem> update(@PathVariable Long id, @RequestBody ServiceItem s) { s.setId(id); return Result.ok(repo.save(s)); }
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) { repo.deleteById(id); return Result.ok(); }
}
