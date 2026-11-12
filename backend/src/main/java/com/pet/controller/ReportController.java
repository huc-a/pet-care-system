package com.pet.controller;
import com.pet.common.*;
import com.pet.entity.*;
import com.pet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ServiceReportRepository reports;
    private final BookingOrderRepository orders;
    @GetMapping("/order/{orderId}") public Result<List<ServiceReport>> byOrder(Authentication auth, @PathVariable Long orderId) {
        BookingOrder o = orders.findById(orderId).orElseThrow(() -> new BizException("订单不存在"));
        Long uid = CurrentUser.id(auth);
        if (CurrentUser.isOwner(auth) && !o.getOwnerId().equals(uid)) throw new BizException("无权查看");
        if (CurrentUser.isSitter(auth) && o.getSitterId() != null && !o.getSitterId().equals(uid)) throw new BizException("无权查看");
        return Result.ok(reports.findByOrderIdOrderByCreateTimeDesc(orderId));
    }
    @PostMapping public Result<ServiceReport> create(Authentication auth, @RequestBody ServiceReport req) {
        BookingOrder o = orders.findById(req.getOrderId()).orElseThrow(() -> new BizException("订单不存在"));
        Long uid = CurrentUser.id(auth);
        if (CurrentUser.isSitter(auth) && (o.getSitterId() == null || !o.getSitterId().equals(uid))) throw new BizException("只能为自己接的订单填写报告");
        if (!List.of("ACCEPTED", "IN_SERVICE").contains(o.getStatus())) throw new BizException("当前订单状态不可填写报告");
        req.setId(null); req.setOrderNo(o.getOrderNo()); req.setSitterId(uid); req.setSitterName(o.getSitterName());
        if (req.getReportDate() == null) req.setReportDate(LocalDate.now());
        if ("ACCEPTED".equals(o.getStatus())) { o.setStatus("IN_SERVICE"); orders.save(o); }
        return Result.ok(reports.save(req));
    }
    @DeleteMapping("/{id}") public Result<Void> delete(@PathVariable Long id) { reports.deleteById(id); return Result.ok(); }
}
