package com.pet.service;
import com.pet.entity.*;
import com.pet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final SitterProfileRepository profiles;
    private final SysUserRepository users;

    public List<Map<String, Object>> match(String area, String breed, String species) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (SitterProfile p : profiles.findByAvailableTrue()) {
            SysUser u = users.findById(p.getUserId()).orElse(null);
            if (u == null || (u.getStatus() != null && u.getStatus() == 0)) continue;
            double score = 0; List<String> reasons = new ArrayList<>();
            if (area != null && !area.isBlank() && area.equals(p.getArea())) { score += 30; reasons.add("同区域（" + area + "），上门距离近"); }
            else reasons.add("服务区域为" + p.getArea());
            double ratingScore = p.getRating() == null ? 0 : p.getRating().doubleValue() * 6;
            score += ratingScore; reasons.add("综合评分 " + p.getRating() + " 分");
            boolean breedHit = breed != null && !breed.isBlank() && p.getSpecialties() != null && p.getSpecialties().contains(breed);
            boolean speciesHit = species != null && !species.isBlank() && p.getPetTypes() != null && p.getPetTypes().contains(species);
            if (breedHit) { score += 20; reasons.add("擅长照护" + breed + "，经验对口"); }
            else if (speciesHit) { score += 12; reasons.add("可照护" + species + "类宠物"); }
            double orderScore = Math.min(10, (p.getOrderCount() == null ? 0 : p.getOrderCount()) / 10.0);
            score += orderScore; reasons.add("累计接单 " + p.getOrderCount() + " 单，经验丰富");
            double distScore = p.getDistanceKm() == null ? 5 : Math.max(0, 10 - p.getDistanceKm().doubleValue());
            score += distScore;
            if (p.getDistanceKm() != null) reasons.add("距您约 " + p.getDistanceKm() + " 公里");
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("sitterId", p.getUserId()); m.put("profileId", p.getId());
            m.put("name", u.getRealName()); m.put("username", u.getUsername()); m.put("avatarUrl", u.getAvatarUrl());
            m.put("area", p.getArea()); m.put("rating", p.getRating()); m.put("orderCount", p.getOrderCount());
            m.put("specialties", p.getSpecialties()); m.put("petTypes", p.getPetTypes());
            m.put("experienceYears", p.getExperienceYears()); m.put("intro", p.getIntro());
            m.put("distanceKm", p.getDistanceKm()); m.put("score", Math.round(score * 10) / 10.0);
            m.put("reasons", reasons);
            out.add(m);
        }
        out.sort((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")));
        return out.size() > 3 ? out.subList(0, 3) : out;
    }
}
