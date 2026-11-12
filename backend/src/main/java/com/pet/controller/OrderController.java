package com.pet.controller;
import com.pet.common.*;
import com.pet.entity.*;
import com.pet.repository.*;
import com.pet.service.PricingService;
import lombok.*;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final BookingOrderRepository orders;
    private final PetRepository pets;
    private final ServiceItemRepository services;
    private final SysUserRepository users;
    private final SitterProfileRepository profiles;
    private final BoardingSpaceRepository spaces;
    private final UserCouponRepository userCoupons;
    private final PaymentRepository payments;
    private final MessageRepository messages;
    private final PricingService pricing;

    @Data public static class CreateReq {
        private Long serviceId; private List<Long> petIds; private Long sitterId; private Long spaceId;
        private LocalDate startDate; private LocalDate endDate; private Integer timesPerDay;
        private List<Long> addonIds; private Long userCouponId; private String address; private String area; private String remark;
    }
    @Data public static class StatusReq { private String status; }
    @Data public static class AssignReq { private Long sitterId; }

    static String genNo() { return "PO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + (int) (Math.random() * 900 + 100); }

    @PostMapping("/price-preview")
    public Result<Map<String, Object>> preview(Authentication auth, @RequestBody CreateReq req) {
        var q = pricing.quote(req.getServiceId(), req.getPetIds(), req.getStartDate(), req.getEndDate(),
            req.getTimesPerDay(), req.getAddonIds(), req.getUserCouponId(), CurrentUser.id(auth));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("baseAmount", q.baseAmount()); m.put("addonAmount", q.addonAmount());
        m.put("holidayFactor", q.holidayFactor()); m.put("holidayAmount", q.holidayAmount());
        m.put("longStayDiscount", q.longStayDiscount()); m.put("multiPetDiscount", q.multiPetDiscount());
        m.put("couponDiscount", q.couponDiscount()); m.put("totalAmount", q.totalAmount());
        m.put("days", q.days()); m.put("quantity", q.quantity()); m.put("detail", q.detail());
        return Result.ok(m);
    }

    @PostMapping @Transactional
    public Result<BookingOrder> create(Authentication auth, @RequestBody CreateReq req) {
        Long uid = CurrentUser.id(auth);
        SysUser me = users.findById(uid).orElseThrow(() -> new BizException("用户不存在"));
        if (req.getPetIds() == null || req.getPetIds().isEmpty()) throw new BizException("请选择宠物");
        List<Pet> myPets = new ArrayList<>();
        for (Long pid : req.getPetIds()) {
            Pet p = pets.findById(pid).orElseThrow(() -> new BizException("宠物不存在: " + pid));
            if (CurrentUser.isOwner(auth) && !p.getOwnerId().equals(uid)) throw new BizException("只能为自己的宠物下单");
            myPets.add(p);
        }
        ServiceItem svc = services.findById(req.getServiceId()).orElseThrow(() -> new BizException("服务不存在"));
        boolean boarding = "BOARDING".equals(svc.getCategory());
        BoardingSpace space = null;
        if (boarding) {
            if (req.getSpaceId() == null) throw new BizException("寄养服务必须选择寄养空间");
            space = spaces.findById(req.getSpaceId()).orElseThrow(() -> new BizException("寄养空间不存在"));
            if (space.getOccupied() != null && space.getTotalCapacity() != null && space.getOccupied() >= space.getTotalCapacity())
                throw new BizException("该寄养空间已满，请选择其他空间");
            for (Pet p : myPets) {
                if (p.getVaccineStatus() == null || !p.getVaccineStatus().contains("齐全"))
                    throw new BizException("宠物「" + p.getName() + "」疫苗未接种齐全，不可寄养");
            }
        }
        var q = pricing.quote(req.getServiceId(), req.getPetIds(), req.getStartDate(), req.getEndDate(),
            req.getTimesPerDay(), req.getAddonIds(), req.getUserCouponId(), uid);
        BookingOrder o = new BookingOrder();
        o.setOrderNo(genNo()); o.setOwnerId(uid); o.setOwnerName(me.getRealName());
        o.setPetIds(req.getPetIds().stream().map(String::valueOf).collect(Collectors.joining(",")));
        o.setPetNames(myPets.stream().map(Pet::getName).collect(Collectors.joining("、")));
        o.setPetCount(myPets.size());
        o.setServiceId(svc.getId()); o.setServiceName(svc.getName()); o.setServiceCategory(svc.getCategory());
        if (req.getSitterId() != null) {
            o.setSitterId(req.getSitterId());
            users.findById(req.getSitterId()).ifPresent(u -> o.setSitterName(u.getRealName()));
            o.setStatus("CONFIRMED");
        } else o.setStatus("PENDING");
        if (space != null) { o.setSpaceId(space.getId()); o.setSpaceName(space.getName()); }
        o.setStartDate(req.getStartDate()); o.setEndDate(req.getEndDate());
        o.setDays(q.days()); o.setTimesPerDay(req.getTimesPerDay() == null ? 1 : req.getTimesPerDay()); o.setQuantity(q.quantity());
        if (req.getAddonIds() != null && !req.getAddonIds().isEmpty()) {
            o.setAddonNames(services.findAllById(req.getAddonIds()).stream().map(ServiceItem::getName).collect(Collectors.joining("、")));
        }
        o.setBaseAmount(q.baseAmount()); o.setAddonAmount(q.addonAmount()); o.setHolidayFactor(q.holidayFactor());
        o.setHolidayAmount(q.holidayAmount()); o.setLongStayDiscount(q.longStayDiscount());
        o.setMultiPetDiscount(q.multiPetDiscount()); o.setCouponDiscount(q.couponDiscount());
        o.setTotalAmount(q.totalAmount()); o.setPriceDetail(q.detail());
        o.setUserCouponId(req.getUserCouponId());
        o.setPayStatus("PAID"); o.setArea(req.getArea() != null ? req.getArea() : me.getArea());
        o.setAddress(req.getAddress()); o.setRemark(req.getRemark());
        orders.save(o);
        if (req.getUserCouponId() != null) {
            userCoupons.findById(req.getUserCouponId()).ifPresent(uc -> { uc.setStatus("USED"); uc.setOrderId(o.getId()); uc.setUseTime(LocalDateTime.now()); userCoupons.save(uc); });
        }
        Payment pay = new Payment();
        pay.setTxnNo("PAY" + o.getOrderNo()); pay.setOrderId(o.getId()); pay.setOrderNo(o.getOrderNo());
        pay.setUserId(uid); pay.setUserName(me.getRealName()); pay.setAmount(q.totalAmount());
        pay.setMethod("微信支付"); pay.setType("PAY"); pay.setStatus("SUCCESS");
        payments.save(pay);
        Message msg = new Message(); msg.setUserId(uid); msg.setTitle("下单成功");
        msg.setContent("订单 " + o.getOrderNo() + " 已创建并支付 ¥" + q.totalAmount() + "，等待确认。"); msg.setType("ORDER");
        messages.save(msg);
        return Result.ok(o);
    }

    private void checkVisible(Authentication auth, BookingOrder o) {
        Long uid = CurrentUser.id(auth);
        if (CurrentUser.isOwner(auth) && !o.getOwnerId().equals(uid)) throw new BizException("无权查看他人订单");
        if (CurrentUser.isSitter(auth) && o.getSitterId() != null && !o.getSitterId().equals(uid)
            && !"CONFIRMED".equals(o.getStatus())) throw new BizException("无权查看该订单");
    }
    @GetMapping public Result<List<BookingOrder>> list(Authentication auth,
            @RequestParam(required = false) String status, @RequestParam(required = false) String keyword) {
        List<BookingOrder> all;
        if (CurrentUser.isOwner(auth)) all = orders.findByOwnerIdOrderByCreateTimeDesc(CurrentUser.id(auth));
        else if (CurrentUser.isSitter(auth)) all = orders.findBySitterIdOrderByCreateTimeDesc(CurrentUser.id(auth));
        else all = orders.findAllByOrderByCreateTimeDesc();
        return Result.ok(filter(all, status, keyword));
    }
    @GetMapping("/hall") public Result<List<BookingOrder>> hall(Authentication auth) {
        List<BookingOrder> all = orders.findByStatusOrderByCreateTimeDesc("CONFIRMED");
        all.removeIf(o -> o.getSitterId() != null);
        return Result.ok(all);
    }
    static List<BookingOrder> filter(List<BookingOrder> all, String status, String keyword) {
        return all.stream()
            .filter(o -> status == null || status.isBlank() || status.equals(o.getStatus()))
            .filter(o -> keyword == null || keyword.isBlank() || o.getOrderNo().contains(keyword)
                || (o.getPetNames() != null && o.getPetNames().contains(keyword))
                || (o.getServiceName() != null && o.getServiceName().contains(keyword)))
            .collect(Collectors.toList());
    }
    @GetMapping("/{id}") public Result<BookingOrder> get(Authentication auth, @PathVariable Long id) {
        BookingOrder o = orders.findById(id).orElseThrow(() -> new BizException("订单不存在"));
        checkVisible(auth, o); return Result.ok(o);
    }
    static final Map<String, List<String>> FLOW = Map.of(
        "PENDING", List.of("CONFIRMED", "CANCELLED"),
        "CONFIRMED", List.of("ACCEPTED", "CANCELLED", "IN_SERVICE"),
        "ACCEPTED", List.of("IN_SERVICE", "CANCELLED"),
        "IN_SERVICE", List.of("COMPLETED"),
        "COMPLETED", List.of("REFUNDED"),
        "CANCELLED", List.of(), "REFUNDED", List.of());
    @PutMapping("/{id}/status") @Transactional
    public Result<BookingOrder> changeStatus(Authentication auth, @PathVariable Long id, @RequestBody StatusReq req) {
        BookingOrder o = orders.findById(id).orElseThrow(() -> new BizException("订单不存在"));
        checkVisible(auth, o);
        String target = req.getStatus();
        if (!FLOW.getOrDefault(o.getStatus(), List.of()).contains(target))
            throw new BizException("订单状态不允许从 " + o.getStatus() + " 流转到 " + target);
        if ("CANCELLED".equals(target) && CurrentUser.isSitter(auth)) throw new BizException("宠托师不能取消订单");
        o.setStatus(target); o.setUpdateTime(LocalDateTime.now());
        if ("REFUNDED".equals(target)) {
            o.setPayStatus("REFUNDED");
            Payment rp = new Payment(); rp.setTxnNo("REF" + o.getOrderNo()); rp.setOrderId(o.getId()); rp.setOrderNo(o.getOrderNo());
            rp.setUserId(o.getOwnerId()); rp.setUserName(o.getOwnerName()); rp.setAmount(o.getTotalAmount());
            rp.setMethod("原路退回"); rp.setType("REFUND"); rp.setStatus("SUCCESS"); payments.save(rp);
        }
        if ("CANCELLED".equals(target)) o.setPayStatus("UNPAID");
        orders.save(o); return Result.ok(o);
    }
    @PutMapping("/{id}/accept") @Transactional
    public Result<BookingOrder> accept(Authentication auth, @PathVariable Long id) {
        if (!CurrentUser.isSitter(auth) && !CurrentUser.isAdmin(auth)) throw new BizException("只有宠托师可以接单");
        BookingOrder o = orders.findById(id).orElseThrow(() -> new BizException("订单不存在"));
        if (!"CONFIRMED".equals(o.getStatus()) || o.getSitterId() != null) throw new BizException("该订单已被接或状态不正确");
        Long uid = CurrentUser.id(auth);
        o.setSitterId(uid); users.findById(uid).ifPresent(u -> o.setSitterName(u.getRealName()));
        o.setStatus("ACCEPTED"); o.setUpdateTime(LocalDateTime.now()); orders.save(o);
        Message msg = new Message(); msg.setUserId(o.getOwnerId()); msg.setTitle("订单已被接单");
        msg.setContent("订单 " + o.getOrderNo() + " 已由宠托师 " + o.getSitterName() + " 接单。"); msg.setType("ORDER"); messages.save(msg);
        return Result.ok(o);
    }
    @PutMapping("/{id}/assign") @Transactional
    public Result<BookingOrder> assign(Authentication auth, @PathVariable Long id, @RequestBody AssignReq req) {
        if (!CurrentUser.isAdmin(auth)) throw new BizException("只有管理员可以指派");
        BookingOrder o = orders.findById(id).orElseThrow(() -> new BizException("订单不存在"));
        o.setSitterId(req.getSitterId());
        users.findById(req.getSitterId()).ifPresent(u -> o.setSitterName(u.getRealName()));
        if ("PENDING".equals(o.getStatus()) || "CONFIRMED".equals(o.getStatus())) o.setStatus("ACCEPTED");
        o.setUpdateTime(LocalDateTime.now()); return Result.ok(orders.save(o));
    }
    @GetMapping("/calendar") public Result<Map<String, Object>> calendar(Authentication auth,
            @RequestParam int year, @RequestParam int month) {
        List<BookingOrder> all = list(auth, null, null).getData();
        Map<String, List<BookingOrder>> byDay = new TreeMap<>();
        for (BookingOrder o : all) {
            LocalDate d = o.getStartDate(); LocalDate end = o.getEndDate() == null ? d : o.getEndDate();
            while (!d.isAfter(end)) {
                if (d.getYear() == year && d.getMonthValue() == month)
                    byDay.computeIfAbsent(d.toString(), k -> new ArrayList<>()).add(o);
                d = d.plusDays(1);
            }
        }
        Map<String, Object> counts = new LinkedHashMap<>();
        byDay.forEach((k, v) -> counts.put(k, v.size()));
        return Result.ok(Map.of("counts", counts, "orders", byDay));
    }
}
