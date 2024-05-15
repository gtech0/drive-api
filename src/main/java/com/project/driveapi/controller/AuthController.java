package com.project.driveapi.controller;

import com.project.driveapi.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/drive")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping(value = "/sign-in")
    public void googleSignIn(HttpServletResponse response) throws Exception {
        authService.googleSignIn(response);
    }

    @GetMapping(value = "/oauth")
    public void saveAuthorizationCode(HttpServletRequest request) throws Exception {
        authService.saveAuthorizationCode(request);
    }

}
