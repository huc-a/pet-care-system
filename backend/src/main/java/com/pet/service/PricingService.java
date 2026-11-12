package com.pet.service;
import com.pet.common.BizException;
import com.pet.entity.*;
import com.pet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PricingService {
    private final ServiceItemRepository services;
    private final CouponRepository coupons;
    private final UserCouponRepository userCoupons;

    public static final Set<LocalDate> HOLIDAYS = new HashSet<>();
    static {
        addRange("2026-01-01", "2026-01-03"); addRange("2026-02-16", "2026-02-22");
        addRange("2026-04-04", "2026-04-06"); addRange("2026-05-01", "2026-05-05");
        addRange("2026-06-19", "2026-06-21"); addRange("2026-09-25", "2026-09-27");
        addRange("2026-10-01", "2026-10-07"); addRange("2025-10-01", "2025-10-08");
        addRange("2025-12-31", "2026-01-03");
    }
    static void addRange(String a, String b) {
        LocalDate d = LocalDate.parse(a), e = LocalDate.parse(b);
        while (!d.isAfter(e)) { HOLIDAYS.add(d); d = d.plusDays(1); }
    }
    public static boolean isHoliday(LocalDate s, LocalDate e) {
        LocalDate d = s; while (!d.isAfter(e)) { if (HOLIDAYS.contains(d)) return true; d = d.plusDays(1); } return false;
    }
    static BigDecimal m(BigDecimal v) { return v.setScale(2, RoundingMode.HALF_UP); }

    public record Quote(BigDecimal baseAmount, BigDecimal addonAmount, BigDecimal holidayFactor, BigDecimal holidayAmount,
                        BigDecimal longStayDiscount, BigDecimal multiPetDiscount, BigDecimal couponDiscount,
                        BigDecimal totalAmount, int days, int quantity, String detail) {}

    public Quote quote(Long serviceId, List<Long> petIds, LocalDate start, LocalDate end, Integer timesPerDay,
                       List<Long> addonIds, Long userCouponId, Long userId) {
        ServiceItem svc = services.findById(serviceId).orElseThrow(() -> new BizException("服务项目不存在"));
        if (end.isBefore(start)) throw new BizException("结束日期不能早于开始日期");
        int days = (int) (end.toEpochDay() - start.toEpochDay()) + 1;
        int tpd = timesPerDay == null ? 1 : Math.max(1, timesPerDay);
        int qty = "PER_DAY".equals(svc.getPricingType()) ? days : days * tpd;
        int petCount = petIds == null ? 1 : Math.max(1, petIds.size());
        BigDecimal unit = svc.getPrice();
        BigDecimal baseGross = unit.multiply(BigDecimal.valueOf((long) qty * petCount));
        BigDecimal multiPetDiscount = petCount > 1
            ? unit.multiply(BigDecimal.valueOf(qty)).multiply(new BigDecimal("0.20")).multiply(BigDecimal.valueOf(petCount - 1))
            : BigDecimal.ZERO;
        BigDecimal addonAmount = BigDecimal.ZERO;
        List<String> addonParts = new ArrayList<>();
        if (addonIds != null) for (Long aid : addonIds) {
            ServiceItem a = services.findById(aid).orElseThrow(() -> new BizException("附加服务不存在: " + aid));
            BigDecimal amt = a.getPrice().multiply(BigDecimal.valueOf(qty));
            addonAmount = addonAmount.add(amt);
            addonParts.add(a.getName() + " ¥" + a.getPrice() + "×" + qty + "=¥" + m(amt));
        }
        BigDecimal subtotal = baseGross.subtract(multiPetDiscount).add(addonAmount);
        BigDecimal factor = isHoliday(start, end) ? new BigDecimal("1.5") : BigDecimal.ONE;
        BigDecimal holidayAmount = subtotal.multiply(factor.subtract(BigDecimal.ONE));
        BigDecimal afterHoliday = subtotal.add(holidayAmount);
        BigDecimal longRate = days >= 15 ? new BigDecimal("0.20") : days >= 7 ? new BigDecimal("0.10") : BigDecimal.ZERO;
        BigDecimal longStayDiscount = afterHoliday.multiply(longRate);
        BigDecimal afterDiscount = afterHoliday.subtract(longStayDiscount);
        BigDecimal couponDiscount = BigDecimal.ZERO;
        String couponDesc = "无";
        if (userCouponId != null) {
            UserCoupon uc = userCoupons.findById(userCouponId).orElseThrow(() -> new BizException("优惠券不存在"));
            if (userId != null && !uc.getUserId().equals(userId)) throw new BizException("优惠券不属于当前用户");
            if (!"UNUSED".equals(uc.getStatus())) throw new BizException("优惠券已使用或已过期");
            Coupon cpn = coupons.findById(uc.getCouponId()).orElseThrow(() -> new BizException("优惠券模板不存在"));
            LocalDate today = LocalDate.now();
            if (cpn.getValidTo() != null && today.isAfter(cpn.getValidTo())) throw new BizException("优惠券已过期");
            if ("FULL_REDUCE".equals(cpn.getType())) {
                if (afterDiscount.compareTo(cpn.getThreshold()) < 0) throw new BizException("未达到优惠券满减门槛 ¥" + cpn.getThreshold());
                couponDiscount = cpn.getOffAmount().min(afterDiscount);
            } else if ("DISCOUNT".equals(cpn.getType()) && cpn.getDiscountRate() != null) {
                couponDiscount = afterDiscount.multiply(BigDecimal.ONE.subtract(cpn.getDiscountRate()));
            }
            couponDesc = cpn.getName() + " -¥" + m(couponDiscount);
        }
        BigDecimal total = afterDiscount.subtract(couponDiscount);
        if (total.compareTo(new BigDecimal("0.01")) < 0) total = new BigDecimal("0.01");
        String detail = String.format("基础价 ¥%s×%d单位×%d只宠物=¥%s；多宠折扣(第2只起8折) -¥%s；附加服务 %s 合计 ¥%s；节假日系数 %s 加价 ¥%s；长住折扣(%d天) -¥%s；优惠券 %s",
            unit, qty, petCount, m(baseGross), m(multiPetDiscount),
            addonParts.isEmpty() ? "无" : String.join("、", addonParts), m(addonAmount),
            factor, m(holidayAmount), days, m(longStayDiscount), couponDesc);
        return new Quote(m(baseGross), m(addonAmount), factor, m(holidayAmount), m(longStayDiscount),
            m(multiPetDiscount), m(couponDiscount), m(total), days, qty, detail);
    }
}
