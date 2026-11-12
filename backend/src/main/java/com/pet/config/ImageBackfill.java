package com.pet.config;
import com.pet.entity.*;
import com.pet.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** 为已上线数据库回填图片字段（仅填充为空的记录，不覆盖用户上传） */
@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class ImageBackfill implements CommandLineRunner {
    private final SysUserRepository users; private final PetRepository pets;
    private final ServiceItemRepository services; private final BoardingSpaceRepository spaces;
    private final ServiceReportRepository reports; private final BookingOrderRepository orders;

    @Override public void run(String... args) {
        int n = 0;
        for (SysUser u : users.findAll()) {
            if (u.getAvatarUrl() == null || u.getAvatarUrl().isBlank()) {
                String a = ImageAssets.AVATAR_BY_USERNAME.get(u.getUsername());
                if (a != null) { u.setAvatarUrl(a); users.save(u); n++; }
            }
        }
        for (Pet p : pets.findAll()) {
            if (p.getPhotoUrl() == null || p.getPhotoUrl().isBlank()) {
                String a = ImageAssets.PET_BY_NAME.getOrDefault(p.getName(), ImageAssets.PET_BY_BREED.get(p.getBreed()));
                if (a != null) { p.setPhotoUrl(a); pets.save(p); n++; }
            }
        }
        for (ServiceItem s : services.findAll()) {
            boolean changed = false;
            if (s.getCoverUrl() == null || s.getCoverUrl().isBlank()) {
                String a = ImageAssets.SERVICE_BY_NAME.get(s.getName());
                if (a != null) { s.setCoverUrl(a); changed = true; }
            }
            if (s.getSalesCount() == null) { s.setSalesCount(ImageAssets.SERVICE_SALES.getOrDefault(s.getName(), 0)); changed = true; }
            if (changed) { services.save(s); n++; }
        }
        for (BoardingSpace s : spaces.findAll()) {
            if (s.getPhotoUrl() == null || s.getPhotoUrl().isBlank()) {
                String a = ImageAssets.SPACE_BY_NAME.get(s.getName());
                if (a != null) { s.setPhotoUrl(a); spaces.save(s); n++; }
            }
        }
        for (ServiceReport r : reports.findAll()) {
            if (r.getPhotoUrl() == null || r.getPhotoUrl().isBlank()) {
                orders.findById(r.getOrderId()).ifPresent(o -> {
                    String a = ImageAssets.SERVICE_BY_NAME.get(o.getServiceName());
                    if (a != null) { r.setPhotoUrl(a); reports.save(r); }
                });
            }
        }
        if (n > 0) log.info("ImageBackfill: filled image fields for {} records", n);
    }
}
