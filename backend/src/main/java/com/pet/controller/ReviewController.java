package com.pet.controller;
import com.pet.common.*;
import com.pet.entity.*;
import com.pet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewRepository reviews;
    private final BookingOrderRepository orders;
    private final SitterProfileRepository profiles;
    @GetMapping public Result<List<Review>> list(Authentication auth,
            @RequestParam(required = false) Long sitterId) {
        List<Review> all;
        if (sitterId != null) all = reviews.findBySitterIdOrderByCreateTimeDesc(sitterId);
        else if (CurrentUser.isOwner(auth)) all = reviews.findByOwnerIdOrderByCreateTimeDesc(CurrentUser.id(auth));
        else if (CurrentUser.isSitter(auth)) all = reviews.findBySitterIdOrderByCreateTimeDesc(CurrentUser.id(auth));
        else all = reviews.findAllByOrderByCreateTimeDesc();
        if (!CurrentUser.isAdmin(auth)) all.removeIf(r -> Boolean.TRUE.equals(r.getHidden()));
        return Result.ok(all);
    }
    @PostMapping @Transactional public Result<Review> create(Authentication auth, @RequestBody Review req) {
        BookingOrder o = orders.findById(req.getOrderId()).orElseThrow(() -> new BizException("订单不存在"));
        if (!"COMPLETED".equals(o.getStatus())) throw new BizException("订单完成后才能评价");
        if (CurrentUser.isOwner(auth) && !o.getOwnerId().equals(CurrentUser.id(auth))) throw new BizException("只能评价自己的订单");
        if (reviews.existsByOrderId(o.getId())) throw new BizException("该订单已评价");
        if (req.getRating() == null || req.getRating() < 1 || req.getRating() > 5) throw new BizException("评分需为1-5星");
        req.setId(null); req.setOrderNo(o.getOrderNo()); req.setOwnerId(o.getOwnerId()); req.setOwnerName(o.getOwnerName());
        req.setSitterId(o.getSitterId()); req.setSitterName(o.getSitterName()); req.setHidden(false);
        reviews.save(req);
        if (o.getSitterId() != null) {
            profiles.findByUserId(o.getSitterId()).ifPresent(p -> {
                List<Review> rs = reviews.findBySitterIdOrderByCreateTimeDesc(o.getSitterId());
                double avg = rs.stream().filter(r -> !Boolean.TRUE.equals(r.getHidden())).mapToInt(Review::getRating).average().orElse(5.0);
                p.setRating(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));
                p.setOrderCount((p.getOrderCount() == null ? 0 : p.getOrderCount()));
                profiles.save(p);
            });
        }
        return Result.ok(req);
    }
    @PutMapping("/{id}/hide") public Result<Review> hide(Authentication auth, @PathVariable Long id) {
        if (!CurrentUser.isAdmin(auth)) throw new BizException("只有管理员可以隐藏评价");
        Review r = reviews.findById(id).orElseThrow(() -> new BizException("评价不存在"));
        r.setHidden(!Boolean.TRUE.equals(r.getHidden())); return Result.ok(reviews.save(r));
    }
    @DeleteMapping("/{id}") public Result<Void> delete(Authentication auth, @PathVariable Long id) {
        if (!CurrentUser.isAdmin(auth)) throw new BizException("只有管理员可以删除评价");
        reviews.deleteById(id); return Result.ok();
    }
}
