package com.pet.controller;
import com.pet.common.*;
import com.pet.entity.*;
import com.pet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeRepository notices;
    private final MessageRepository messages;
    @GetMapping public Result<List<Notice>> list() { return Result.ok(notices.findAllByOrderByCreateTimeDesc()); }
    @PostMapping public Result<Notice> create(Authentication auth, @RequestBody Notice n) {
        if (!CurrentUser.isAdmin(auth)) throw new BizException("只有管理员可以发布公告");
        n.setId(null); n.setPublisher(auth.getName()); return Result.ok(notices.save(n)); }
    @PutMapping("/{id}") public Result<Notice> update(Authentication auth, @PathVariable Long id, @RequestBody Notice n) {
        if (!CurrentUser.isAdmin(auth)) throw new BizException("只有管理员可以修改");
        n.setId(id); return Result.ok(notices.save(n)); }
    @DeleteMapping("/{id}") public Result<Void> delete(Authentication auth, @PathVariable Long id) {
        if (!CurrentUser.isAdmin(auth)) throw new BizException("只有管理员可以删除");
        notices.deleteById(id); return Result.ok(); }
    @GetMapping("/messages/mine") public Result<List<Message>> myMessages(Authentication auth) {
        return Result.ok(messages.findByUserIdOrderByCreateTimeDesc(CurrentUser.id(auth))); }
    @PutMapping("/messages/{id}/read") public Result<Void> read(Authentication auth, @PathVariable Long id) {
        messages.findById(id).ifPresent(m -> { m.setReadFlag(true); messages.save(m); }); return Result.ok(); }
}
