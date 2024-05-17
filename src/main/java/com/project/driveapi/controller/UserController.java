package com.project.driveapi.controller;

import com.project.driveapi.dto.AboutDto;
import com.project.driveapi.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/drive")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping(value = "/sign-in")
    public void googleSignIn(HttpServletResponse response) throws Exception {
        userService.googleSignIn(response);
    }

    @GetMapping(value = "/oauth")
    public void saveAuthorizationCode(HttpServletRequest request) throws Exception {
        userService.saveAuthorizationCode(request);
    }

    @GetMapping(value = "/about")
    public AboutDto getAboutInfo() throws Exception {
        return userService.getAboutInfo();
    }

}
