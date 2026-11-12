package com.pet.controller;
import com.pet.common.*;
import com.pet.entity.*;
import com.pet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentRepository payments;
    @GetMapping public Result<List<Payment>> list(Authentication auth) {
        if (CurrentUser.isAdmin(auth)) return Result.ok(payments.findAllByOrderByCreateTimeDesc());
        return Result.ok(payments.findByUserIdOrderByCreateTimeDesc(CurrentUser.id(auth)));
    }
}
