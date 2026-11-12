package com.pet.controller;
import com.pet.common.*;
import com.pet.entity.*;
import com.pet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
@RestController
@RequestMapping("/api/tracks")
@RequiredArgsConstructor
public class TrackController {
    private final WalkTrackRepository tracks;
    private final BookingOrderRepository orders;
    public static double haversine(double lng1, double lat1, double lng2, double lat2) {
        double r = 6371.0, dLat = Math.toRadians(lat2 - lat1), dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * r * Math.asin(Math.sqrt(a));
    }
    public static List<double[]> parse(String points) {
        List<double[]> out = new ArrayList<>();
        try {
            var arr = new com.fasterxml.jackson.databind.ObjectMapper().readTree(points);
            for (var n : arr) out.add(new double[]{n.get("lng").asDouble(), n.get("lat").asDouble()});
        } catch (Exception ignored) {}
        return out;
    }
    public static BigDecimal distanceOf(String points) {
        List<double[]> pts = parse(points); double d = 0;
        for (int i = 1; i < pts.size(); i++) d += haversine(pts.get(i-1)[0], pts.get(i-1)[1], pts.get(i)[0], pts.get(i)[1]);
        return BigDecimal.valueOf(d).setScale(2, RoundingMode.HALF_UP);
    }
    @GetMapping("/order/{orderId}") public Result<List<WalkTrack>> byOrder(@PathVariable Long orderId) {
        return Result.ok(tracks.findByOrderId(orderId)); }
    @PostMapping public Result<WalkTrack> create(Authentication auth, @RequestBody WalkTrack req) {
        BookingOrder o = orders.findById(req.getOrderId()).orElseThrow(() -> new BizException("订单不存在"));
        req.setId(null); req.setOrderNo(o.getOrderNo()); req.setSitterId(CurrentUser.id(auth));
        if (req.getPetName() == null) req.setPetName(o.getPetNames());
        if (req.getWalkDate() == null) req.setWalkDate(LocalDate.now());
        BigDecimal dist = distanceOf(req.getPoints()); req.setDistanceKm(dist);
        if (req.getDurationMin() != null && req.getDurationMin() > 0 && dist.doubleValue() > 0)
            req.setPaceMinPerKm(BigDecimal.valueOf(req.getDurationMin() / dist.doubleValue()).setScale(1, RoundingMode.HALF_UP));
        return Result.ok(tracks.save(req));
    }
}
