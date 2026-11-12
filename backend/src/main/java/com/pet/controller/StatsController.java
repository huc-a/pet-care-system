package com.pet.controller;
import com.pet.common.*;
import com.pet.entity.*;
import com.pet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {
    private final BookingOrderRepository orders;
    private final SysUserRepository users;
    private final PetRepository pets;
    private final SitterProfileRepository profiles;
    private final BoardingSpaceRepository spaces;
    private final ReviewRepository reviews;
    private final PaymentRepository payments;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard(Authentication auth) {
        Long uid = CurrentUser.id(auth);
        List<BookingOrder> all = orders.findAllByOrderByCreateTimeDesc();
        List<BookingOrder> mine = all;
        if (CurrentUser.isOwner(auth)) mine = all.stream().filter(o -> o.getOwnerId().equals(uid)).collect(Collectors.toList());
        if (CurrentUser.isSitter(auth)) mine = all.stream().filter(o -> uid.equals(o.getSitterId())).collect(Collectors.toList());
        BigDecimal revenue = payments.findAll().stream()
            .map(p -> "REFUND".equals(p.getType()) ? p.getAmount().negate() : p.getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal mySpend = mine.stream().filter(o -> !"CANCELLED".equals(o.getStatus())).map(BookingOrder::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        // 近30天趋势
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 29; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            List<BookingOrder> day = mine.stream().filter(o -> o.getCreateTime() != null && o.getCreateTime().toLocalDate().equals(d)).collect(Collectors.toList());
            BigDecimal rev = day.stream().filter(o -> !"CANCELLED".equals(o.getStatus())).map(BookingOrder::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> m = new LinkedHashMap<>(); m.put("date", d.toString().substring(5)); m.put("orders", day.size()); m.put("revenue", rev);
            trend.add(m);
        }
        Map<String, Long> serviceDist = mine.stream().collect(Collectors.groupingBy(BookingOrder::getServiceName, Collectors.counting()));
        Map<String, Long> statusDist = mine.stream().collect(Collectors.groupingBy(BookingOrder::getStatus, Collectors.counting()));
        List<Pet> petScope = CurrentUser.isOwner(auth) ? pets.findByOwnerId(uid) : pets.findAll();
        Map<String, Long> speciesDist = petScope.stream().collect(Collectors.groupingBy(Pet::getSpecies, Collectors.counting()));
        List<Map<String, Object>> topSitters = new ArrayList<>();
        profiles.findAll().stream().sorted(Comparator.comparing(p -> -(p.getOrderCount() == null ? 0 : p.getOrderCount()))).limit(5)
            .forEach(p -> { SysUser u = users.findById(p.getUserId()).orElse(null);
                Map<String, Object> m = new LinkedHashMap<>(); m.put("name", u == null ? "宠托师" : u.getRealName()); m.put("orders", p.getOrderCount()); m.put("rating", p.getRating()); topSitters.add(m); });
        int totalCap = spaces.findAll().stream().mapToInt(BoardingSpace::getTotalCapacity).sum();
        int occ = spaces.findAll().stream().mapToInt(s -> s.getOccupied() == null ? 0 : s.getOccupied()).sum();
        double occupancyRate = totalCap == 0 ? 0 : Math.round(occ * 1000.0 / totalCap) / 10.0;
        List<Review> rs = reviews.findAll();
        double goodRate = rs.isEmpty() ? 100 : Math.round(rs.stream().filter(r -> r.getRating() >= 4).count() * 1000.0 / rs.size()) / 10.0;
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("totalOrders", mine.size());
        out.put("totalRevenue", CurrentUser.isAdmin(auth) ? revenue : mySpend);
        out.put("totalPets", petScope.size());
        out.put("totalUsers", users.count());
        out.put("totalSitters", profiles.count());
        out.put("pendingCount", mine.stream().filter(o -> "PENDING".equals(o.getStatus())).count());
        out.put("inServiceCount", mine.stream().filter(o -> "IN_SERVICE".equals(o.getStatus())).count());
        out.put("completedCount", mine.stream().filter(o -> "COMPLETED".equals(o.getStatus())).count());
        out.put("occupancyRate", occupancyRate); out.put("occupied", occ); out.put("totalCapacity", totalCap);
        out.put("goodRate", goodRate); out.put("reviewCount", rs.size());
        out.put("trend", trend); out.put("serviceDist", serviceDist); out.put("statusDist", statusDist);
        out.put("speciesDist", speciesDist); out.put("topSitters", topSitters);
        return Result.ok(out);
    }
}
