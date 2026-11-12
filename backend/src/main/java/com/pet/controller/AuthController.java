package com.pet.controller;
import com.pet.common.*;
import com.pet.entity.*;
import com.pet.repository.*;
import com.pet.security.JwtUtil;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final SysUserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;
    @Data public static class LoginReq { @NotBlank private String username; @NotBlank private String password; }
    @Data public static class RegisterReq { @NotBlank private String username; @NotBlank private String password;
        @NotBlank private String realName; private String phone; private String email; private String area; }
    @Data public static class ChangePwdReq { private String oldPassword; private String newPassword; }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginReq req) {
        SysUser u = users.findByUsername(req.getUsername()).orElseThrow(() -> new BizException("用户名或密码错误"));
        if (u.getStatus() != null && u.getStatus() == 0) throw new BizException("账号已被禁用");
        if (!encoder.matches(req.getPassword(), u.getPassword())) throw new BizException("用户名或密码错误");
        return Result.ok(Map.of("token", jwt.generate(u.getUsername(), u.getRole(), u.getId()), "user", u));
    }
    @PostMapping("/register")
    public Result<SysUser> register(@RequestBody RegisterReq req) {
        if (users.existsByUsername(req.getUsername())) throw new BizException("用户名已被注册");
        SysUser u = new SysUser();
        u.setUsername(req.getUsername()); u.setPassword(encoder.encode(req.getPassword()));
        u.setRealName(req.getRealName()); u.setRole("OWNER"); u.setPhone(req.getPhone());
        u.setEmail(req.getEmail()); u.setArea(req.getArea()); u.setAvatarEmoji("🐾"); u.setAvatarUrl("/img/avatar-default.jpg");
        users.save(u);
        return Result.ok(u);
    }
    @GetMapping("/me")
    public Result<SysUser> me(Authentication auth) {
        return Result.ok(users.findByUsername(auth.getName()).orElseThrow(() -> new BizException("用户不存在")));
    }
    @PutMapping("/profile")
    public Result<SysUser> profile(Authentication auth, @RequestBody SysUser req) {
        SysUser u = users.findByUsername(auth.getName()).orElseThrow(() -> new BizException("用户不存在"));
        if (req.getRealName() != null) u.setRealName(req.getRealName());
        if (req.getPhone() != null) u.setPhone(req.getPhone());
        if (req.getEmail() != null) u.setEmail(req.getEmail());
        if (req.getArea() != null) u.setArea(req.getArea());
        if (req.getAvatarEmoji() != null) u.setAvatarEmoji(req.getAvatarEmoji());
        if (req.getAvatarUrl() != null) u.setAvatarUrl(req.getAvatarUrl());
        users.save(u); return Result.ok(u);
    }
    @PostMapping("/change-password")
    public Result<Void> changePwd(Authentication auth, @RequestBody ChangePwdReq req) {
        SysUser u = users.findByUsername(auth.getName()).orElseThrow(() -> new BizException("用户不存在"));
        if (!encoder.matches(req.getOldPassword(), u.getPassword())) throw new BizException("原密码不正确");
        u.setPassword(encoder.encode(req.getNewPassword())); users.save(u); return Result.ok();
    }
}
