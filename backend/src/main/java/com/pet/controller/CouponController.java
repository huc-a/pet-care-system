package com.pet.controller;
import com.pet.common.*;
import com.pet.entity.*;
import com.pet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponRepository coupons;
    private final UserCouponRepository userCoupons;
    @GetMapping public Result<List<Coupon>> list() { return Result.ok(coupons.findAll()); }
    @PostMapping public Result<Coupon> create(Authentication auth, @RequestBody Coupon c) {
        if (!CurrentUser.isAdmin(auth)) throw new BizException("只有管理员可以发放优惠券");
        c.setId(null); if (c.getClaimedCount() == null) c.setClaimedCount(0); return Result.ok(coupons.save(c)); }
    @PutMapping("/{id}") public Result<Coupon> update(Authentication auth, @PathVariable Long id, @RequestBody Coupon c) {
        if (!CurrentUser.isAdmin(auth)) throw new BizException("只有管理员可以修改");
        c.setId(id); return Result.ok(coupons.save(c)); }
    @DeleteMapping("/{id}") public Result<Void> delete(Authentication auth, @PathVariable Long id) {
        if (!CurrentUser.isAdmin(auth)) throw new BizException("只有管理员可以删除");
        coupons.deleteById(id); return Result.ok(); }
    @PostMapping("/{id}/claim") public Result<UserCoupon> claim(Authentication auth, @PathVariable Long id) {
        Coupon c = coupons.findById(id).orElseThrow(() -> new BizException("优惠券不存在"));
        Long uid = CurrentUser.id(auth);
        if (userCoupons.existsByCouponIdAndUserId(id, uid)) throw new BizException("已领取过该优惠券");
        if (c.getTotalCount() != null && c.getTotalCount() > 0 && c.getClaimedCount() >= c.getTotalCount()) throw new BizException("优惠券已领完");
        UserCoupon uc = new UserCoupon(); uc.setCouponId(id); uc.setUserId(uid); uc.setStatus("UNUSED");
        userCoupons.save(uc);
        c.setClaimedCount(c.getClaimedCount() + 1); coupons.save(c);
        return Result.ok(uc);
    }
    @GetMapping("/mine") public Result<List<Map<String, Object>>> mine(Authentication auth) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (UserCoupon uc : userCoupons.findByUserId(CurrentUser.id(auth))) {
            Coupon c = coupons.findById(uc.getCouponId()).orElse(null); if (c == null) continue;
            String st = uc.getStatus();
            if ("UNUSED".equals(st) && c.getValidTo() != null && LocalDate.now().isAfter(c.getValidTo())) st = "EXPIRED";
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userCoupon", uc); m.put("coupon", c); m.put("effectiveStatus", st);
            out.add(m);
        }
        return Result.ok(out);
    }
}
