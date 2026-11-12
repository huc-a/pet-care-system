package com.pet.config;
import com.pet.entity.*;
import com.pet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final SysUserRepository users; private final PetRepository pets;
    private final ServiceItemRepository services; private final SitterProfileRepository profiles;
    private final BoardingSpaceRepository spaces; private final BookingOrderRepository orders;
    private final CheckinRecordRepository checkins; private final ServiceReportRepository reports;
    private final WalkTrackRepository tracks; private final HealthRecordRepository health;
    private final ReviewRepository reviews; private final CouponRepository coupons;
    private final UserCouponRepository userCoupons; private final PaymentRepository payments;
    private final NoticeRepository notices; private final MessageRepository messages;
    private final PasswordEncoder encoder;

    BigDecimal bd(String s) { return new BigDecimal(s); }
    SysUser user(String un, String pw, String name, String role, String phone, String area, String emoji) {
        SysUser u = new SysUser(); u.setUsername(un); u.setPassword(encoder.encode(pw)); u.setRealName(name);
        u.setRole(role); u.setPhone(phone); u.setArea(area); u.setAvatarEmoji(emoji); u.setEmail(un + "@pet.com");
        u.setAvatarUrl(ImageAssets.AVATAR_BY_USERNAME.get(un));
        return users.save(u);
    }
    Pet pet(Long owner, String name, String sp, String breed, String gender, int age, String w, boolean neut,
            String color, String pers, String allergy, String feed, String vaccine, String emoji) {
        Pet p = new Pet(); p.setOwnerId(owner); p.setName(name); p.setSpecies(sp); p.setBreed(breed); p.setGender(gender);
        p.setAgeMonths(age); p.setWeight(bd(w)); p.setNeutered(neut); p.setColor(color); p.setPersonality(pers);
        p.setAllergy(allergy); p.setFeedingHabit(feed); p.setHospital("爱宠动物医院"); p.setVaccineStatus(vaccine); p.setEmoji(emoji);
        p.setPhotoUrl(ImageAssets.PET_BY_NAME.getOrDefault(name, ImageAssets.PET_BY_BREED.get(breed)));
        return pets.save(p);
    }
    ServiceItem svc(String name, String cat, String type, String price, int dur, String desc, String icon) {
        ServiceItem s = new ServiceItem(); s.setName(name); s.setCategory(cat); s.setPricingType(type);
        s.setPrice(bd(price)); s.setDurationMin(dur); s.setDescription(desc); s.setIcon(icon);
        s.setCoverUrl(ImageAssets.SERVICE_BY_NAME.get(name)); s.setSalesCount(ImageAssets.SERVICE_SALES.getOrDefault(name, 0)); return services.save(s);
    }
    SitterProfile prof(Long uid, String area, String spec, String types, int exp, String intro, String rating, int cnt, String dist) {
        SitterProfile p = new SitterProfile(); p.setUserId(uid); p.setArea(area); p.setSpecialties(spec); p.setPetTypes(types);
        p.setExperienceYears(exp); p.setIntro(intro); p.setRating(bd(rating)); p.setOrderCount(cnt); p.setAvailable(true);
        p.setCertStatus("已认证"); p.setDistanceKm(bd(dist)); return profiles.save(p);
    }
    BoardingSpace space(String name, String type, int total, int occ, String price, String fac, String size, String desc) {
        BoardingSpace s = new BoardingSpace(); s.setName(name); s.setType(type); s.setTotalCapacity(total); s.setOccupied(occ);
        s.setPricePerDay(bd(price)); s.setFacilities(fac); s.setSizeDesc(size); s.setDescription(desc);
        s.setPhotoUrl(ImageAssets.SPACE_BY_NAME.get(name)); return spaces.save(s);
    }
    int seq = 1000;
    BookingOrder order(SysUser owner, String petNames, String petIds, int petCount, ServiceItem svc, SysUser sitter,
                       BoardingSpace space, LocalDate start, LocalDate end, int tpd, String status, String total,
                       LocalDateTime created, String area, String addr) {
        BookingOrder o = new BookingOrder();
        o.setOrderNo("PO2026" + (seq++)); o.setOwnerId(owner.getId()); o.setOwnerName(owner.getRealName());
        o.setPetNames(petNames); o.setPetIds(petIds); o.setPetCount(petCount);
        o.setServiceId(svc.getId()); o.setServiceName(svc.getName()); o.setServiceCategory(svc.getCategory());
        if (sitter != null) { o.setSitterId(sitter.getId()); o.setSitterName(sitter.getRealName()); }
        if (space != null) { o.setSpaceId(space.getId()); o.setSpaceName(space.getName()); }
        o.setStartDate(start); o.setEndDate(end);
        int days = (int) (end.toEpochDay() - start.toEpochDay()) + 1; o.setDays(days); o.setTimesPerDay(tpd);
        o.setQuantity("PER_DAY".equals(svc.getPricingType()) ? days : days * tpd);
        BigDecimal tot = bd(total); o.setTotalAmount(tot); o.setBaseAmount(tot);
        o.setStatus(status); o.setPayStatus("CANCELLED".equals(status) ? "UNPAID" : "REFUNDED".equals(status) ? "REFUNDED" : "PAID");
        o.setArea(area); o.setAddress(addr); o.setCreateTime(created); o.setUpdateTime(created);
        o.setPriceDetail("种子订单，金额按定价规则核算");
        orders.save(o);
        if (!"CANCELLED".equals(status)) {
            Payment pay = new Payment(); pay.setTxnNo("PAY" + o.getOrderNo()); pay.setOrderId(o.getId()); pay.setOrderNo(o.getOrderNo());
            pay.setUserId(owner.getId()); pay.setUserName(owner.getRealName()); pay.setAmount(tot);
            pay.setMethod("微信支付"); pay.setType("PAY"); pay.setStatus("SUCCESS"); pay.setCreateTime(created.plusMinutes(2)); payments.save(pay);
        }
        if ("REFUNDED".equals(status)) {
            Payment rp = new Payment(); rp.setTxnNo("REF" + o.getOrderNo()); rp.setOrderId(o.getId()); rp.setOrderNo(o.getOrderNo());
            rp.setUserId(owner.getId()); rp.setUserName(owner.getRealName()); rp.setAmount(tot);
            rp.setMethod("原路退回"); rp.setType("REFUND"); rp.setStatus("SUCCESS"); rp.setCreateTime(created.plusDays(1)); payments.save(rp);
        }
        return o;
    }
    String trackPoints(double lng0, double lat0, int n, double step) {
        StringBuilder sb = new StringBuilder("[");
        Random r = new Random(42);
        for (int i = 0; i < n; i++) {
            double lng = lng0 + step * i + (r.nextDouble() - 0.5) * step * 0.8;
            double lat = lat0 + Math.sin(i * 0.7) * step * 2.2 + step * i * 0.35;
            if (i > 0) sb.append(",");
            sb.append(String.format(Locale.US, "{\"lng\":%.6f,\"lat\":%.6f,\"t\":%d}", lng, lat, i * 60));
        }
        return sb.append("]").toString();
    }

    @Override public void run(String... args) {
        if (users.count() > 0) return;
        SysUser admin = user("admin", "admin123", "平台管理员", "ADMIN", "13800000000", "朝阳区", "🦊");
        SysUser owner = user("owner", "owner123", "林小满", "OWNER", "13911112222", "朝阳区", "🐰");
        SysUser owner2 = user("owner2", "owner123", "陈志强", "OWNER", "13933334444", "海淀区", "🐻");
        SysUser owner3 = user("owner3", "owner123", "王悦", "OWNER", "13955556666", "西城区", "🦄");
        SysUser sitter = user("sitter", "sitter123", "苏晴", "SITTER", "13711110001", "朝阳区", "🐱");
        SysUser sitter2 = user("sitter2", "sitter123", "赵一鸣", "SITTER", "13711110002", "海淀区", "🐶");
        SysUser sitter3 = user("sitter3", "sitter123", "刘晓彤", "SITTER", "13711110003", "朝阳区", "🐹");
        SysUser sitter4 = user("sitter4", "sitter123", "周子航", "SITTER", "13711110004", "西城区", "🐼");
        SysUser sitter5 = user("sitter5", "sitter123", "吴佳怡", "SITTER", "13711110005", "丰台区", "🦉");
        SysUser sitter6 = user("sitter6", "sitter123", "郑浩然", "SITTER", "13711110006", "通州区", "🐯");
        prof(sitter.getId(), "朝阳区", "金毛,英短,布偶", "猫,狗", 5, "5年宠物照护经验，持有宠物护理员证书，温柔耐心，擅长大型犬与布偶猫。", "4.9", 186, "1.2");
        prof(sitter2.getId(), "海淀区", "柯基,边牧,柴犬", "狗", 4, "退役训犬师助理，擅长中大型犬遛弯与行为引导。", "4.8", 152, "2.5");
        prof(sitter3.getId(), "朝阳区", "英短,美短,布偶", "猫", 6, "资深猫奴，自家养3只猫，熟悉猫咪应激护理与喂药。", "5.0", 231, "0.8");
        prof(sitter4.getId(), "西城区", "泰迪,比熊,博美", "狗", 3, "擅长小型犬洗护与陪玩，服务细致。", "4.7", 98, "3.1");
        prof(sitter5.getId(), "丰台区", "金毛,拉布拉多,萨摩耶", "狗", 4, "热爱大狗，体力充沛，每日可遛10公里。", "4.8", 134, "4.0");
        prof(sitter6.getId(), "通州区", "英短,中华田园猫,柯基", "猫,狗", 2, "细心负责，提供全程照片与视频汇报。", "4.6", 76, "5.2");

        Pet doudou = pet(owner.getId(), "豆豆", "狗", "金毛", "公", 36, "28.5", true, "金色", "温顺亲人，喜欢接飞盘", "无", "每日两餐，狗粮150g/餐，饭后遛弯30分钟", "已接种齐全", "🐕");
        Pet naicha = pet(owner.getId(), "奶茶", "猫", "英短", "母", 24, "5.2", true, "蓝灰", "安静粘人，怕生", "对鸡肉轻微过敏", "猫粮自由采食，每日一个罐头", "已接种齐全", "🐈");
        Pet tuantuan = pet(owner.getId(), "团团", "猫", "布偶", "母", 18, "4.6", false, "双色", "温柔好撸，喜欢逗猫棒", "无", "每日两餐，冻干拌粮", "已接种齐全", "🐱");
        Pet gulu = pet(owner.getId(), "咕噜", "猫", "美短", "公", 30, "5.8", true, "虎斑", "活泼好动，好奇心强", "无", "每日两餐，晚间加餐", "已接种齐全", "🐈");
        Pet wangcai = pet(owner2.getId(), "旺财", "狗", "柯基", "公", 28, "12.4", true, "黄白", "精力旺盛，爱拆家需多运动", "无", "每日两餐，控制食量防胖", "已接种齐全", "🐶");
        Pet xueqiu = pet(owner2.getId(), "雪球", "狗", "比熊", "母", 20, "6.1", false, "白色", "胆小粘人，怕打雷", "对牛肉过敏", "每日三餐少量多餐", "已接种齐全", "🐩");
        Pet afu = pet(owner2.getId(), "阿福", "狗", "边牧", "公", 40, "19.8", true, "黑白", "聪明好学，需要高强度运动", "无", "每日两餐，运动后加餐", "已接种齐全", "🐕");
        Pet zhima = pet(owner3.getId(), "芝麻", "狗", "柴犬", "母", 26, "9.6", true, "赤色", "独立有主见，不爱洗澡", "无", "每日两餐", "已接种齐全", "🐕");
        Pet buding = pet(owner3.getId(), "布丁", "狗", "泰迪", "公", 48, "4.2", true, "棕色", "聪明粘人，喜欢抱抱", "无", "每日两餐，老年犬粮", "已接种齐全", "🐩");
        Pet xiaoju = pet(owner3.getId(), "小橘", "猫", "中华田园猫", "公", 15, "4.9", false, "橘色", "能吃能睡，性格稳定", "无", "干粮为主，每周两次罐头", "已接种齐全", "🐈");

        ServiceItem feedCat = svc("上门喂猫", "VISIT", "PER_TIME", "45", 30, "上门喂食、换水、铲屎、陪玩，附图文报告", "🐈");
        ServiceItem feedDog = svc("上门喂狗", "VISIT", "PER_TIME", "55", 40, "上门喂食、换水、简单清洁，附图文报告", "🐕");
        ServiceItem walkDog = svc("上门遛狗", "WALK", "PER_TIME", "40", 45, "上门牵遛45分钟，GPS轨迹全程可查", "🦮");
        ServiceItem boardCat = svc("门店寄养-猫", "BOARDING", "PER_DAY", "88", 1440, "独立猫别墅，每日日报+视频看护", "🏠");
        ServiceItem boardDog = svc("门店寄养-狗", "BOARDING", "PER_DAY", "128", 1440, "阳光犬舍，每日两次户外活动", "🏡");
        ServiceItem boardHome = svc("家庭寄养", "BOARDING", "PER_DAY", "98", 1440, "宠托师家庭寄养，散养不关笼", "🛋️");
        ServiceItem bath = svc("洗护美容", "GROOMING", "PER_TIME", "120", 90, "洗澡、吹干、修剪指甲与脚底毛", "🛁");
        ServiceItem play = svc("陪玩服务", "VISIT", "PER_TIME", "30", 30, "专人陪玩互动，消耗精力解无聊", "🎾");
        ServiceItem care = svc("喂药护理", "VISIT", "PER_TIME", "35", 20, "按医嘱喂药、滴眼药、伤口护理", "💊");
        svc("加餐营养餐", "ADDON", "PER_TIME", "15", 0, "鸡胸肉/三文鱼营养加餐（附加服务）", "🍗");
        svc("接送服务", "ADDON", "PER_TIME", "30", 0, "专车上门接送宠物（附加服务）", "🚗");
        svc("视频陪伴", "ADDON", "PER_TIME", "20", 0, "服务全程视频直播陪伴（附加服务）", "📹");
        ServiceItem medAddon = svc("上门喂药加项", "ADDON", "PER_TIME", "10", 0, "在基础上门服务中增加喂药（附加服务）", "💉");

        BoardingSpace catA = space("猫咪别墅A区", "猫别墅", 12, 3, "88", "独立玻璃房、猫爬架、恒温新风、24h监控", "每间约3㎡", "专为猫咪设计的安静别墅区");
        BoardingSpace dogSun = space("阳光犬舍", "犬舍", 10, 2, "128", "落地窗阳光房、户外草坪、每日两次放风", "每间约5㎡", "大中型犬首选，活动空间充足");
        BoardingSpace homeSeat = space("家庭寄养位", "家庭寄养", 6, 1, "98", "沙发散养、专人陪睡、家庭氛围", "整户散养", "像在自己家一样自在");
        BoardingSpace luxury = space("豪华单间", "豪华房", 4, 1, "198", "独立卫浴、宠物床、摄像头一对一", "约8㎡", "尊贵单间，适合多宠家庭");
        BoardingSpace catB = space("猫咪别墅B区", "猫别墅", 8, 0, "78", "多层跳台、自动饮水机、费洛蒙安抚", "每间约2.5㎡", "性价比猫咪寄养区");
        BoardingSpace pupPark = space("小型犬乐园", "犬舍", 8, 0, "108", "室内恒温乐园、玩具池、同伴社交", "每间约4㎡", "小型犬社交乐园");
        LocalDate today = LocalDate.now();
        BookingOrder o1 = order(owner, "豆豆", "" + doudou.getId(), 1, walkDog, sitter, null, today.minusDays(2), today.minusDays(2), 1, "COMPLETED", "40", LocalDateTime.now().minusDays(2).withHour(9), "朝阳区", "朝阳区望京花园1号楼");
        BookingOrder o2 = order(owner, "奶茶、团团", naicha.getId() + "," + tuantuan.getId(), 2, feedCat, sitter3, null, today.minusDays(5), today.minusDays(3), 1, "COMPLETED", "243", LocalDateTime.now().minusDays(6).withHour(10), "朝阳区", "朝阳区望京花园1号楼");
        BookingOrder o3 = order(owner, "团团", "" + tuantuan.getId(), 1, boardCat, sitter3, catA, today.minusDays(1), today.plusDays(3), 1, "IN_SERVICE", "440", LocalDateTime.now().minusDays(4).withHour(11), "朝阳区", "平台门店");
        BookingOrder o4 = order(owner2, "旺财", "" + wangcai.getId(), 1, walkDog, sitter2, null, today.minusDays(1), today.plusDays(2), 2, "IN_SERVICE", "320", LocalDateTime.now().minusDays(3).withHour(8), "海淀区", "海淀区中关村大街27号");
        BookingOrder o5 = order(owner2, "阿福", "" + afu.getId(), 1, boardDog, sitter5, dogSun, today.minusDays(2), today.plusDays(2), 1, "IN_SERVICE", "640", LocalDateTime.now().minusDays(5).withHour(14), "海淀区", "平台门店");
        BookingOrder o6 = order(owner3, "芝麻", "" + zhima.getId(), 1, boardHome, sitter4, homeSeat, today, today.plusDays(4), 1, "IN_SERVICE", "490", LocalDateTime.now().minusDays(2).withHour(16), "西城区", "宠托师家");
        BookingOrder o7 = order(owner, "咕噜", "" + gulu.getId(), 1, feedCat, null, null, today.plusDays(1), today.plusDays(3), 1, "CONFIRMED", "135", LocalDateTime.now().minusDays(1).withHour(9), "朝阳区", "朝阳区望京花园1号楼");
        BookingOrder o8 = order(owner2, "雪球", "" + xueqiu.getId(), 1, bath, null, null, today.plusDays(2), today.plusDays(2), 1, "CONFIRMED", "120", LocalDateTime.now().withHour(9), "海淀区", "平台门店");
        BookingOrder o9 = order(owner3, "布丁、小橘", buding.getId() + "," + xiaoju.getId(), 2, feedDog, null, null, today.plusDays(3), today.plusDays(5), 1, "CONFIRMED", "297", LocalDateTime.now().withHour(10), "西城区", "西城区金融街15号");
        BookingOrder o10 = order(owner, "豆豆", "" + doudou.getId(), 1, feedDog, null, null, today.plusDays(1), today.plusDays(1), 1, "PENDING", "55", LocalDateTime.now().withHour(11), "朝阳区", "朝阳区望京花园1号楼");
        BookingOrder o11 = order(owner3, "芝麻", "" + zhima.getId(), 1, walkDog, null, null, today.plusDays(2), today.plusDays(4), 1, "PENDING", "120", LocalDateTime.now().withHour(12), "西城区", "西城区金融街15号");
        BookingOrder o12 = order(owner2, "旺财、阿福", wangcai.getId() + "," + afu.getId(), 2, boardDog, null, dogSun, today.plusDays(5), today.plusDays(9), 1, "PENDING", "1152", LocalDateTime.now().withHour(13), "海淀区", "平台门店");
        BookingOrder o13 = order(owner, "奶茶", "" + naicha.getId(), 1, boardCat, sitter3, catA, today.plusDays(1), today.plusDays(6), 1, "ACCEPTED", "528", LocalDateTime.now().minusDays(1).withHour(15), "朝阳区", "平台门店");
        BookingOrder o14 = order(owner2, "雪球", "" + xueqiu.getId(), 1, play, sitter4, null, today.plusDays(1), today.plusDays(1), 2, "ACCEPTED", "60", LocalDateTime.now().withHour(8), "海淀区", "海淀区中关村大街27号");
        BookingOrder o15 = order(owner3, "布丁", "" + buding.getId(), 1, care, sitter, null, today, today.plusDays(2), 1, "ACCEPTED", "105", LocalDateTime.now().minusHours(3), "西城区", "西城区金融街15号");
        BookingOrder o16 = order(owner, "豆豆、咕噜", doudou.getId() + "," + gulu.getId(), 2, feedDog, sitter, null, today.minusDays(10), today.minusDays(8), 1, "COMPLETED", "297", LocalDateTime.now().minusDays(11).withHour(9), "朝阳区", "朝阳区望京花园1号楼");
        BookingOrder o17 = order(owner2, "旺财", "" + wangcai.getId(), 1, feedDog, sitter2, null, today.minusDays(12), today.minusDays(12), 2, "COMPLETED", "110", LocalDateTime.now().minusDays(12).withHour(9), "海淀区", "海淀区中关村大街27号");
        BookingOrder o18 = order(owner3, "小橘", "" + xiaoju.getId(), 1, boardCat, sitter6, catB, today.minusDays(15), today.minusDays(10), 1, "COMPLETED", "468", LocalDateTime.now().minusDays(16).withHour(10), "通州区", "平台门店");
        BookingOrder o19 = order(owner, "团团", "" + tuantuan.getId(), 1, bath, sitter4, null, today.minusDays(7), today.minusDays(7), 1, "COMPLETED", "120", LocalDateTime.now().minusDays(7).withHour(13), "朝阳区", "平台门店");
        BookingOrder o20 = order(owner2, "阿福", "" + afu.getId(), 1, walkDog, sitter5, null, today.minusDays(20), today.minusDays(18), 1, "COMPLETED", "120", LocalDateTime.now().minusDays(20).withHour(9), "丰台区", "丰台区方庄路1号");
        BookingOrder o21 = order(owner, "豆豆", "" + doudou.getId(), 1, boardDog, sitter, luxury, today.minusDays(25), today.minusDays(22), 1, "COMPLETED", "792", LocalDateTime.now().minusDays(26).withHour(10), "朝阳区", "平台门店");
        BookingOrder o22 = order(owner3, "芝麻", "" + zhima.getId(), 1, feedDog, null, null, today.minusDays(3), today.minusDays(3), 1, "CANCELLED", "55", LocalDateTime.now().minusDays(3).withHour(18), "西城区", "西城区金融街15号");
        BookingOrder o23 = order(owner2, "雪球", "" + xueqiu.getId(), 1, boardHome, null, homeSeat, today.minusDays(6), today.minusDays(4), 1, "CANCELLED", "294", LocalDateTime.now().minusDays(6).withHour(19), "海淀区", "宠托师家");
        BookingOrder o24 = order(owner, "奶茶", "" + naicha.getId(), 1, feedCat, sitter3, null, today.minusDays(8), today.minusDays(8), 1, "REFUNDED", "45", LocalDateTime.now().minusDays(8).withHour(9), "朝阳区", "朝阳区望京花园1号楼");
        BookingOrder o25 = order(owner3, "小橘", "" + xiaoju.getId(), 1, feedCat, sitter6, null, today.minusDays(28), today.minusDays(27), 1, "COMPLETED", "90", LocalDateTime.now().minusDays(28).withHour(9), "通州区", "通州区新华大街58号");

        // 入住记录（与占用数一致）
        CheckinRecord c1 = new CheckinRecord(); c1.setOrderId(o3.getId()); c1.setOrderNo(o3.getOrderNo()); c1.setSpaceId(catA.getId()); c1.setSpaceName(catA.getName()); c1.setPetNames("团团"); c1.setCheckinTime(LocalDateTime.now().minusDays(1).withHour(10)); c1.setStatus("IN"); c1.setOperator("sitter"); checkins.save(c1);
        CheckinRecord c2 = new CheckinRecord(); c2.setOrderId(o5.getId()); c2.setOrderNo(o5.getOrderNo()); c2.setSpaceId(dogSun.getId()); c2.setSpaceName(dogSun.getName()); c2.setPetNames("阿福"); c2.setCheckinTime(LocalDateTime.now().minusDays(2).withHour(11)); c2.setStatus("IN"); c2.setOperator("sitter5"); checkins.save(c2);
        CheckinRecord c3 = new CheckinRecord(); c3.setOrderId(o6.getId()); c3.setOrderNo(o6.getOrderNo()); c3.setSpaceId(homeSeat.getId()); c3.setSpaceName(homeSeat.getName()); c3.setPetNames("芝麻"); c3.setCheckinTime(LocalDateTime.now().withHour(9)); c3.setStatus("IN"); c3.setOperator("sitter4"); checkins.save(c3);
        CheckinRecord c4 = new CheckinRecord(); c4.setOrderId(o21.getId()); c4.setOrderNo(o21.getOrderNo()); c4.setSpaceId(luxury.getId()); c4.setSpaceName(luxury.getName()); c4.setPetNames("豆豆"); c4.setCheckinTime(LocalDateTime.now().minusDays(25).withHour(10)); c4.setCheckoutTime(LocalDateTime.now().minusDays(22).withHour(10)); c4.setStatus("OUT"); c4.setOperator("sitter"); checkins.save(c4);
        // 补两条在住以匹配占用：catA 还差2、dogSun 还差1 —— 用历史已完成订单的在住演示记录
        CheckinRecord c5 = new CheckinRecord(); c5.setOrderId(o18.getId()); c5.setOrderNo(o18.getOrderNo()); c5.setSpaceId(catA.getId()); c5.setSpaceName(catA.getName()); c5.setPetNames("奶茶（演示在住）"); c5.setCheckinTime(LocalDateTime.now().minusHours(5)); c5.setStatus("IN"); c5.setOperator("admin"); checkins.save(c5);
        CheckinRecord c6 = new CheckinRecord(); c6.setOrderId(o18.getId()); c6.setOrderNo(o18.getOrderNo()); c6.setSpaceId(catA.getId()); c6.setSpaceName(catA.getName()); c6.setPetNames("咕噜（演示在住）"); c6.setCheckinTime(LocalDateTime.now().minusHours(4)); c6.setStatus("IN"); c6.setOperator("admin"); checkins.save(c6);
        CheckinRecord c7 = new CheckinRecord(); c7.setOrderId(o20.getId()); c7.setOrderNo(o20.getOrderNo()); c7.setSpaceId(dogSun.getId()); c7.setSpaceName(dogSun.getName()); c7.setPetNames("旺财（演示在住）"); c7.setCheckinTime(LocalDateTime.now().minusHours(3)); c7.setStatus("IN"); c7.setOperator("admin"); checkins.save(c7);
        // luxury 占用1
        CheckinRecord c8 = new CheckinRecord(); c8.setOrderId(o19.getId()); c8.setOrderNo(o19.getOrderNo()); c8.setSpaceId(luxury.getId()); c8.setSpaceName(luxury.getName()); c8.setPetNames("团团（演示在住）"); c8.setCheckinTime(LocalDateTime.now().minusHours(2)); c8.setStatus("IN"); c8.setOperator("admin"); checkins.save(c8);

        // 服务报告
        Object[][] reps = {
            {o1, sitter, today.minusDays(2), "食欲旺盛", "活泼", "正常", "38.5", "豆豆今天状态特别好，遛弯时全程兴奋，完成了喂食和饮水补充。"},
            {o2, sitter3, today.minusDays(4), "食欲良好", "安静", "正常", "38.2", "奶茶和团团都已喂食，猫砂已清理，陪玩了20分钟。"},
            {o3, sitter3, today.minusDays(1), "食欲良好", "放松", "正常", "38.4", "团团入住第一天适应良好，晚餐吃完，精神状态放松。"},
            {o3, sitter3, today, "食欲旺盛", "活泼", "正常", "38.3", "团团今日日报：上午晒太阳，下午陪玩逗猫棒，食欲旺盛。"},
            {o5, sitter5, today.minusDays(1), "食欲旺盛", "兴奋", "正常", "38.6", "阿福户外活动两小时，体力充沛，晚餐加餐已完成。"},
            {o16, sitter, today.minusDays(9), "食欲良好", "平稳", "正常", "38.4", "豆豆与咕噜喂食完成，咕噜罐头已开，饮水已更换。"},
            {o18, sitter6, today.minusDays(12), "食欲良好", "安静", "正常", "38.1", "小橘寄养日报：一切正常，喜欢趴在窗台晒太阳。"},
            {o20, sitter5, today.minusDays(19), "食欲旺盛", "兴奋", "正常", "38.7", "阿福遛弯完成，轨迹已上传，全程状态良好。"},
        };
        for (Object[] r : reps) {
            BookingOrder oo = (BookingOrder) r[0]; SysUser ss = (SysUser) r[1];
            ServiceReport rep = new ServiceReport(); rep.setOrderId(oo.getId()); rep.setOrderNo(oo.getOrderNo());
            rep.setSitterId(ss.getId()); rep.setSitterName(ss.getRealName()); rep.setReportDate((LocalDate) r[2]);
            rep.setFed(true); rep.setWaterChanged(true); rep.setLitterCleaned(true); rep.setPlayed(true);
            rep.setWalked(oo.getServiceName().contains("遛")); rep.setAppetite((String) r[3]); rep.setSpirit((String) r[4]);
            rep.setExcretion((String) r[5]); rep.setTemperature(bd((String) r[6])); rep.setContent((String) r[7]);
            reports.save(rep);
        }
        // 遛狗轨迹
        Object[][] trs = { {o1, sitter, "豆豆", 32, today.minusDays(2)}, {o4, sitter2, "旺财", 45, today.minusDays(1)},
            {o20, sitter5, "阿福", 50, today.minusDays(19)}, {o16, sitter, "豆豆", 28, today.minusDays(9)} };
        for (Object[] t : trs) {
            BookingOrder oo = (BookingOrder) t[0];
            WalkTrack wt = new WalkTrack(); wt.setOrderId(oo.getId()); wt.setOrderNo(oo.getOrderNo());
            wt.setSitterId(((SysUser) t[1]).getId()); wt.setPetName((String) t[2]);
            String pts = trackPoints(116.48, 39.99, 24, 0.0016);
            wt.setPoints(pts); wt.setDurationMin((Integer) t[3]); wt.setWalkDate((LocalDate) t[4]);
            java.math.BigDecimal dist = com.pet.controller.TrackController.distanceOf(pts);
            wt.setDistanceKm(dist);
            if (dist.doubleValue() > 0) wt.setPaceMinPerKm(java.math.BigDecimal.valueOf((Integer) t[3] / dist.doubleValue()).setScale(1, java.math.RoundingMode.HALF_UP));
            tracks.save(wt);
        }
        // 健康记录（含30天内到期提醒）
        Object[][] hs = {
            {doudou, "疫苗", "狂犬疫苗", today.minusDays(300), today.plusDays(12), null, "年度加强针将到期"},
            {doudou, "驱虫", "体外驱虫", today.minusDays(20), today.plusDays(10), null, "福来恩体外驱虫"},
            {doudou, "体重", "月度称重", today.minusDays(60), null, "27.9", "体重稳定"},
            {doudou, "体重", "月度称重", today.minusDays(30), null, "28.2", "略有上升"},
            {doudou, "体重", "月度称重", today, null, "28.5", "当前体重"},
            {naicha, "疫苗", "猫三联", today.minusDays(340), today.plusDays(25), null, "猫三联年度接种"},
            {naicha, "驱虫", "体内驱虫", today.minusDays(80), today.plusDays(5), null, "海乐妙体内驱虫"},
            {tuantuan, "疫苗", "猫三联+狂犬", today.minusDays(200), today.plusDays(60), null, "接种齐全"},
            {wangcai, "驱虫", "体外驱虫", today.minusDays(28), today.plusDays(2), null, "体外驱虫快到期"},
            {xueqiu, "疫苗", "六联疫苗", today.minusDays(150), today.plusDays(90), null, "接种齐全"},
            {zhima, "体检", "年度体检", today.minusDays(100), today.plusDays(265), "9.6", "体检一切正常"},
            {xiaoju, "驱虫", "体内外驱虫", today.minusDays(85), today.minusDays(3), null, "已过期3天，需尽快驱虫"},
            {buding, "体检", "老年犬体检", today.minusDays(40), today.plusDays(20), "4.2", "关节注意保暖"},
        };
        for (Object[] h : hs) {
            Pet pp = (Pet) h[0]; HealthRecord hr = new HealthRecord();
            hr.setPetId(pp.getId()); hr.setPetName(pp.getName()); hr.setType((String) h[1]); hr.setName((String) h[2]);
            hr.setRecordDate((LocalDate) h[3]); hr.setNextDueDate((LocalDate) h[4]);
            if (h[5] != null) hr.setWeight(bd((String) h[5]));
            hr.setNote((String) h[6]); health.save(hr);
        }
        // 评价
        Object[][] rvs = {
            {o1, owner, sitter, 5, "苏晴姐姐特别细心，豆豆回家还一直摇尾巴，轨迹和报告都很详细！", "细心,准时,报告详细"},
            {o2, owner, sitter3, 5, "两只猫照顾得无微不至，照片拍得超可爱，下次还找晓彤。", "专业,爱猫,照片好看"},
            {o16, owner, sitter, 5, "连续三天都很准时，咕噜这种怕生的猫都被照顾得很好。", "准时,温柔"},
            {o17, owner2, sitter2, 4, "遛狗很专业，旺财回来累得倒头就睡，好评。", "专业,体力好"},
            {o18, owner3, sitter6, 5, "寄养期间每天都有日报，小橘胖了半斤，太放心了。", "日报详细,负责"},
            {o20, owner2, sitter5, 5, "阿福这种精力怪都能遛到位，佳怡体力太好了。", "遛狗专业"},
            {o21, owner, sitter, 4, "豪华单间环境很好，豆豆住得很开心，就是价格小贵。", "环境好"},
            {o25, owner3, sitter6, 5, "上门喂猫准时准点，小橘状态很好。", "准时"},
        };
        for (Object[] r : rvs) {
            BookingOrder oo = (BookingOrder) r[0]; SysUser ow = (SysUser) r[1]; SysUser st = (SysUser) r[2];
            Review rv = new Review(); rv.setOrderId(oo.getId()); rv.setOrderNo(oo.getOrderNo());
            rv.setOwnerId(ow.getId()); rv.setOwnerName(ow.getRealName()); rv.setSitterId(st.getId()); rv.setSitterName(st.getRealName());
            rv.setRating((Integer) r[3]); rv.setContent((String) r[4]); rv.setTags((String) r[5]); rv.setHidden(false);
            rv.setCreateTime(oo.getCreateTime().plusDays(1)); reviews.save(rv);
        }
        // 优惠券
        Coupon cp1 = new Coupon(); cp1.setName("新人专享满100减20"); cp1.setType("FULL_REDUCE"); cp1.setThreshold(bd("100")); cp1.setOffAmount(bd("20")); cp1.setTotalCount(200); cp1.setClaimedCount(2); cp1.setValidFrom(today.minusDays(10)); cp1.setValidTo(today.plusDays(50)); coupons.save(cp1);
        Coupon cp2 = new Coupon(); cp2.setName("寄养专享9折券"); cp2.setType("DISCOUNT"); cp2.setThreshold(bd("0")); cp2.setDiscountRate(bd("0.90")); cp2.setTotalCount(100); cp2.setClaimedCount(1); cp2.setValidFrom(today.minusDays(5)); cp2.setValidTo(today.plusDays(25)); coupons.save(cp2);
        Coupon cp3 = new Coupon(); cp3.setName("满300减50大额券"); cp3.setType("FULL_REDUCE"); cp3.setThreshold(bd("300")); cp3.setOffAmount(bd("50")); cp3.setTotalCount(50); cp3.setClaimedCount(1); cp3.setValidFrom(today.minusDays(3)); cp3.setValidTo(today.plusDays(27)); coupons.save(cp3);
        UserCoupon uc1 = new UserCoupon(); uc1.setCouponId(cp1.getId()); uc1.setUserId(owner.getId()); uc1.setStatus("UNUSED"); userCoupons.save(uc1);
        UserCoupon uc2 = new UserCoupon(); uc2.setCouponId(cp2.getId()); uc2.setUserId(owner.getId()); uc2.setStatus("UNUSED"); userCoupons.save(uc2);
        UserCoupon uc3 = new UserCoupon(); uc3.setCouponId(cp3.getId()); uc3.setUserId(owner2.getId()); uc3.setStatus("UNUSED"); userCoupons.save(uc3);
        UserCoupon uc4 = new UserCoupon(); uc4.setCouponId(cp1.getId()); uc4.setUserId(owner.getId()); uc4.setStatus("USED"); uc4.setOrderId(o16.getId()); uc4.setUseTime(o16.getCreateTime()); userCoupons.save(uc4);
        // 公告与消息
        String[][] nts = {
            {"春节寄养预订开启通知", "春节假期（2月16日-22日）寄养名额有限，节假日服务价格按1.5系数执行，请提前预订锁定名额。", "平台公告"},
            {"新功能上线：智能匹配宠托师", "下单时可使用智能匹配，系统将按区域、评分、擅长品种与距离为您推荐最合适的宠托师。", "功能更新"},
            {"疫苗接种提醒服务上线", "宠物疫苗与驱虫到期前30天将自动提醒，请及时为爱宠安排接种与驱虫。", "服务通知"},
        };
        for (String[] n : nts) { Notice nt = new Notice(); nt.setTitle(n[0]); nt.setContent(n[1]); nt.setType(n[2]); nt.setPublisher("admin"); notices.save(nt); }
        Message m1 = new Message(); m1.setUserId(owner.getId()); m1.setTitle("欢迎来到宠物寄养平台"); m1.setContent("您好，林小满！豆豆的狂犬疫苗将在12天后到期，请留意健康提醒。"); m1.setType("SYSTEM"); messages.save(m1);
        Message m2 = new Message(); m2.setUserId(sitter.getId()); m2.setTitle("新订单提醒"); m2.setContent("接单大厅有新的已确认订单，可前往抢单。"); m2.setType("ORDER"); messages.save(m2);
        System.out.println("[PetCare] seed data initialized: users=" + users.count() + ", pets=" + pets.count() + ", orders=" + orders.count());
    }
}
