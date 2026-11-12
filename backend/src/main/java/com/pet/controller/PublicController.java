package com.pet.controller;
import com.pet.common.*;
import com.pet.entity.*;
import com.pet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 游客公开只读接口（免登录）。
 * 仅暴露商城浏览所需数据；宠托师信息走公开 DTO，绝不包含手机号/邮箱/用户名/密码。
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {
    private final ServiceItemRepository services;
    private final SitterProfileRepository profiles;
    private final SysUserRepository users;
    private final BoardingSpaceRepository spaces;
    private final ReviewRepository reviews;
    private final NoticeRepository notices;
    private final CouponRepository coupons;
    private final BookingOrderRepository orders;

    @GetMapping("/services")
    public Result<List<ServiceItem>> services() { return Result.ok(services.findAll()); }

    @GetMapping("/services/{id}")
    public Result<ServiceItem> service(@PathVariable Long id) {
        return Result.ok(services.findById(id).orElseThrow(() -> new BizException("服务不存在")));
    }

    /** 宠托师公开信息：只含昵称、头像与档案公开字段，不含 phone/email/username */
    private Map<String, Object> publicSitter(SitterProfile p) {
        SysUser u = users.findById(p.getUserId()).orElse(null);
        if (u == null) return null;
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", u.getId());
        user.put("realName", u.getRealName());
        user.put("avatarEmoji", u.getAvatarEmoji());
        user.put("avatarUrl", u.getAvatarUrl());
        user.put("area", u.getArea());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("profile", p);
        m.put("user", user);
        return m;
    }

    @GetMapping("/sitters")
    public Result<List<Map<String, Object>>> sitters() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (SitterProfile p : profiles.findAll()) {
            Map<String, Object> m = publicSitter(p);
            if (m != null) out.add(m);
        }
        return Result.ok(out);
    }

    @GetMapping("/spaces")
    public Result<List<BoardingSpace>> spaces() { return Result.ok(spaces.findAll()); }

    /** 公开评价：仅未隐藏评价，只给昵称/宠物服务名等公开展示字段 */
    @GetMapping("/reviews")
    public Result<List<Map<String, Object>>> reviews() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Review r : reviews.findAllByOrderByCreateTimeDesc()) {
            if (Boolean.TRUE.equals(r.getHidden())) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("ownerName", r.getOwnerName());
            m.put("sitterName", r.getSitterName());
            m.put("rating", r.getRating());
            m.put("content", r.getContent());
            m.put("tags", r.getTags());
            m.put("createTime", r.getCreateTime());
            out.add(m);
            if (out.size() >= 30) break;
        }
        return Result.ok(out);
    }

    @GetMapping("/notices")
    public Result<List<Notice>> notices() { return Result.ok(notices.findAllByOrderByCreateTimeDesc()); }

    @GetMapping("/coupons")
    public Result<List<Coupon>> coupons() { return Result.ok(coupons.findAll()); }

    /** 游客首页概览：仅聚合数字，不含任何明细数据 */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        List<SitterProfile> ps = profiles.findAll();
        double avg = ps.isEmpty() ? 5.0 : ps.stream().map(p -> p.getRating() == null ? BigDecimal.ZERO : p.getRating())
            .mapToDouble(BigDecimal::doubleValue).average().orElse(5.0);
        int totalSales = services.findAll().stream().mapToInt(s -> s.getSalesCount() == null ? 0 : s.getSalesCount()).sum();
        long totalOrders = orders.count();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("serviceCount", services.count());
        out.put("sitterCount", ps.size());
        out.put("avgRating", BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));
        out.put("totalOrders", totalOrders);
        out.put("totalSales", totalSales);
        out.put("spaceCount", spaces.count());
        return Result.ok(out);
    }
}
