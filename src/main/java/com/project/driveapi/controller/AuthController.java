package com.project.driveapi.controller;

import com.project.driveapi.service.DriveService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/drive")
@RequiredArgsConstructor
public class AuthController {

    private final DriveService driveService;

    @GetMapping(value = "/sign-in")
    public void googleSignIn(HttpServletResponse response) throws Exception {
        driveService.googleSignIn(response);
    }

    @GetMapping(value = "/oauth")
    public String saveAuthorizationCode(HttpServletRequest request) throws Exception {
        return driveService.saveAuthorizationCode(request);
    }

}
