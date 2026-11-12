package com.pet.controller;
import com.pet.common.*;
import com.pet.entity.*;
import com.pet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {
    private final HealthRecordRepository records;
    private final PetRepository pets;
    @GetMapping("/pet/{petId}") public Result<List<HealthRecord>> byPet(Authentication auth, @PathVariable Long petId) {
        Pet p = pets.findById(petId).orElseThrow(() -> new BizException("宠物不存在"));
        if (CurrentUser.isOwner(auth) && !p.getOwnerId().equals(CurrentUser.id(auth))) throw new BizException("无权查看");
        return Result.ok(records.findByPetIdOrderByRecordDateDesc(petId));
    }
    @PostMapping public Result<HealthRecord> create(Authentication auth, @RequestBody HealthRecord req) {
        Pet p = pets.findById(req.getPetId()).orElseThrow(() -> new BizException("宠物不存在"));
        if (CurrentUser.isOwner(auth) && !p.getOwnerId().equals(CurrentUser.id(auth))) throw new BizException("无权操作");
        req.setId(null); req.setPetName(p.getName());
        if (req.getWeight() != null && "体重".equals(req.getType())) { p.setWeight(req.getWeight()); pets.save(p); }
        return Result.ok(records.save(req));
    }
    @DeleteMapping("/{id}") public Result<Void> delete(@PathVariable Long id) { records.deleteById(id); return Result.ok(); }
    @GetMapping("/reminders") public Result<List<Map<String, Object>>> reminders(Authentication auth) {
        LocalDate now = LocalDate.now();
        List<HealthRecord> due = records.findByNextDueDateBetween(now.minusDays(365), now.plusDays(30));
        Long uid = CurrentUser.id(auth);
        List<Map<String, Object>> out = new ArrayList<>();
        for (HealthRecord r : due) {
            if (r.getNextDueDate() == null || r.getNextDueDate().isBefore(now.minusDays(0)) && r.getNextDueDate().isBefore(now)) { /* past due also shown below */ }
            Pet p = pets.findById(r.getPetId()).orElse(null); if (p == null) continue;
            if (CurrentUser.isOwner(auth) && !p.getOwnerId().equals(uid)) continue;
            long daysLeft = r.getNextDueDate().toEpochDay() - now.toEpochDay();
            if (daysLeft > 30) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("record", r); m.put("petName", p.getName()); m.put("species", p.getSpecies()); m.put("breed", p.getBreed());
            m.put("daysLeft", daysLeft); m.put("overdue", daysLeft < 0);
            m.put("text", daysLeft < 0 ? "已过期 " + (-daysLeft) + " 天，请尽快安排" : daysLeft == 0 ? "今天到期" : daysLeft + " 天后到期");
            out.add(m);
        }
        out.sort(Comparator.comparingLong(m -> (Long) m.get("daysLeft")));
        return Result.ok(out);
    }
    @GetMapping("/weight-trend/{petId}") public Result<List<Map<String, Object>>> weightTrend(@PathVariable Long petId) {
        List<Map<String, Object>> out = new ArrayList<>();
        records.findByPetIdOrderByRecordDateDesc(petId).stream()
            .filter(r -> r.getWeight() != null)
            .sorted(Comparator.comparing(HealthRecord::getRecordDate))
            .forEach(r -> out.add(Map.of("date", String.valueOf(r.getRecordDate()), "weight", r.getWeight())));
        return Result.ok(out);
    }
}
