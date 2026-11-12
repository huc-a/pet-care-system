package com.pet.controller;
import com.pet.common.*;
import com.pet.entity.*;
import com.pet.repository.*;
import com.pet.service.MatchService;
import lombok.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/api/sitters")
@RequiredArgsConstructor
public class SitterController {
    private final SitterProfileRepository profiles;
    private final SysUserRepository users;
    private final MatchService matchService;
    @Data public static class MatchReq { private String area; private String breed; private String species; }

    @GetMapping public Result<List<Map<String, Object>>> list() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (SitterProfile p : profiles.findAll()) {
            SysUser u = users.findById(p.getUserId()).orElse(null); if (u == null) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("profile", p); m.put("user", u); out.add(m);
        }
        return Result.ok(out);
    }
    @GetMapping("/{userId}") public Result<Map<String, Object>> detail(@PathVariable Long userId) {
        SitterProfile p = profiles.findByUserId(userId).orElseThrow(() -> new BizException("宠托师不存在"));
        SysUser u = users.findById(userId).orElseThrow(() -> new BizException("用户不存在"));
        return Result.ok(Map.of("profile", p, "user", u));
    }
    @PostMapping("/match") public Result<List<Map<String, Object>>> match(@RequestBody MatchReq req) {
        return Result.ok(matchService.match(req.getArea(), req.getBreed(), req.getSpecies()));
    }
    @PutMapping("/my-profile") public Result<SitterProfile> myProfile(Authentication auth, @RequestBody SitterProfile req) {
        SitterProfile p = profiles.findByUserId(CurrentUser.id(auth)).orElseThrow(() -> new BizException("宠托师档案不存在"));
        if (req.getArea() != null) p.setArea(req.getArea());
        if (req.getSpecialties() != null) p.setSpecialties(req.getSpecialties());
        if (req.getPetTypes() != null) p.setPetTypes(req.getPetTypes());
        if (req.getIntro() != null) p.setIntro(req.getIntro());
        if (req.getAvailable() != null) p.setAvailable(req.getAvailable());
        if (req.getExperienceYears() != null) p.setExperienceYears(req.getExperienceYears());
        return Result.ok(profiles.save(p));
    }
    @PutMapping("/{id}") public Result<SitterProfile> update(@PathVariable Long id, @RequestBody SitterProfile req) {
        SitterProfile p = profiles.findById(id).orElseThrow(() -> new BizException("档案不存在"));
        req.setId(id); req.setUserId(p.getUserId());
        return Result.ok(profiles.save(req));
    }
}
