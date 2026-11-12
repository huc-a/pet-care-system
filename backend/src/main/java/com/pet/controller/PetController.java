package com.pet.controller;
import com.pet.common.*;
import com.pet.entity.*;
import com.pet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {
    private final PetRepository pets;
    @GetMapping public Result<List<Pet>> list(Authentication auth) {
        Long uid = CurrentUser.id(auth);
        if (CurrentUser.isOwner(auth)) return Result.ok(pets.findByOwnerId(uid));
        return Result.ok(pets.findAll());
    }
    @GetMapping("/{id}") public Result<Pet> get(Authentication auth, @PathVariable Long id) {
        Pet p = pets.findById(id).orElseThrow(() -> new BizException("宠物不存在"));
        if (CurrentUser.isOwner(auth) && !p.getOwnerId().equals(CurrentUser.id(auth))) throw new BizException("无权查看他人宠物");
        return Result.ok(p);
    }
    @PostMapping public Result<Pet> create(Authentication auth, @RequestBody Pet p) {
        p.setId(null);
        if (CurrentUser.isOwner(auth)) p.setOwnerId(CurrentUser.id(auth));
        if (p.getOwnerId() == null) throw new BizException("请选择宠物主人");
        return Result.ok(pets.save(p));
    }
    @PutMapping("/{id}") public Result<Pet> update(Authentication auth, @PathVariable Long id, @RequestBody Pet req) {
        Pet p = pets.findById(id).orElseThrow(() -> new BizException("宠物不存在"));
        if (CurrentUser.isOwner(auth) && !p.getOwnerId().equals(CurrentUser.id(auth))) throw new BizException("无权修改他人宠物");
        req.setId(id); req.setOwnerId(p.getOwnerId()); req.setCreateTime(p.getCreateTime());
        return Result.ok(pets.save(req));
    }
    @DeleteMapping("/{id}") public Result<Void> delete(Authentication auth, @PathVariable Long id) {
        Pet p = pets.findById(id).orElseThrow(() -> new BizException("宠物不存在"));
        if (CurrentUser.isOwner(auth) && !p.getOwnerId().equals(CurrentUser.id(auth))) throw new BizException("无权删除他人宠物");
        pets.deleteById(id); return Result.ok();
    }
}
