package com.pet.controller;
import com.pet.common.*;
import com.pet.entity.*;
import com.pet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/api/spaces")
@RequiredArgsConstructor
public class SpaceController {
    private final BoardingSpaceRepository spaces;
    private final CheckinRecordRepository checkins;
    @GetMapping public Result<List<BoardingSpace>> list() { return Result.ok(spaces.findAll()); }
    @GetMapping("/{id}/guests") public Result<List<CheckinRecord>> guests(@PathVariable Long id) {
        List<CheckinRecord> out = new ArrayList<>();
        for (CheckinRecord r : checkins.findByStatus("IN")) if (id.equals(r.getSpaceId())) out.add(r);
        return Result.ok(out);
    }
    @PostMapping @PreAuthorize("hasRole('ADMIN')")
    public Result<BoardingSpace> create(@RequestBody BoardingSpace s) { s.setId(null); if (s.getOccupied() == null) s.setOccupied(0); return Result.ok(spaces.save(s)); }
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public Result<BoardingSpace> update(@PathVariable Long id, @RequestBody BoardingSpace req) {
        BoardingSpace s = spaces.findById(id).orElseThrow(() -> new BizException("空间不存在"));
        req.setId(id); req.setOccupied(s.getOccupied()); return Result.ok(spaces.save(req)); }
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) { spaces.deleteById(id); return Result.ok(); }
}
