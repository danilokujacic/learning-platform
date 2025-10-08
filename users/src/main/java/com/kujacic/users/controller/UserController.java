package com.kujacic.users.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")  // ← This path matters for Gateway routing!
public class UserController {

    @GetMapping("/me")
    public void getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        // TO BE IMPLEMENTED
    }
}
