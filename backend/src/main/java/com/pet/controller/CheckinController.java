package com.pet.controller;
import com.pet.common.*;
import com.pet.entity.*;
import com.pet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
@RestController
@RequestMapping("/api/checkins")
@RequiredArgsConstructor
public class CheckinController {
    private final CheckinRecordRepository checkins;
    private final BookingOrderRepository orders;
    private final BoardingSpaceRepository spaces;
    @GetMapping public Result<List<CheckinRecord>> list() { return Result.ok(checkins.findAll()); }
    @GetMapping("/order/{orderId}") public Result<List<CheckinRecord>> byOrder(@PathVariable Long orderId) {
        return Result.ok(checkins.findByOrderId(orderId)); }
    @PostMapping("/order/{orderId}/checkin") @Transactional
    public Result<CheckinRecord> checkin(Authentication auth, @PathVariable Long orderId) {
        BookingOrder o = orders.findById(orderId).orElseThrow(() -> new BizException("订单不存在"));
        if (o.getSpaceId() == null) throw new BizException("该订单不是寄养订单");
        if (!List.of("CONFIRMED", "ACCEPTED").contains(o.getStatus())) throw new BizException("当前状态不可入住: " + o.getStatus());
        if (checkins.findByOrderIdAndStatus(orderId, "IN").isPresent()) throw new BizException("该订单已入住");
        BoardingSpace s = spaces.findById(o.getSpaceId()).orElseThrow(() -> new BizException("寄养空间不存在"));
        if (s.getOccupied() >= s.getTotalCapacity()) throw new BizException("寄养空间已满，无法入住");
        s.setOccupied(s.getOccupied() + 1); spaces.save(s);
        CheckinRecord r = new CheckinRecord();
        r.setOrderId(o.getId()); r.setOrderNo(o.getOrderNo()); r.setSpaceId(s.getId()); r.setSpaceName(s.getName());
        r.setPetNames(o.getPetNames()); r.setCheckinTime(LocalDateTime.now()); r.setStatus("IN");
        r.setOperator(auth.getName()); checkins.save(r);
        o.setStatus("IN_SERVICE"); o.setUpdateTime(LocalDateTime.now()); orders.save(o);
        return Result.ok(r);
    }
    @PostMapping("/order/{orderId}/checkout") @Transactional
    public Result<CheckinRecord> checkout(Authentication auth, @PathVariable Long orderId) {
        BookingOrder o = orders.findById(orderId).orElseThrow(() -> new BizException("订单不存在"));
        CheckinRecord r = checkins.findByOrderIdAndStatus(orderId, "IN").orElseThrow(() -> new BizException("未找到在住记录"));
        BoardingSpace s = spaces.findById(r.getSpaceId()).orElseThrow(() -> new BizException("寄养空间不存在"));
        s.setOccupied(Math.max(0, s.getOccupied() - 1)); spaces.save(s);
        r.setCheckoutTime(LocalDateTime.now()); r.setStatus("OUT"); checkins.save(r);
        o.setStatus("COMPLETED"); o.setUpdateTime(LocalDateTime.now()); orders.save(o);
        return Result.ok(r);
    }
}
