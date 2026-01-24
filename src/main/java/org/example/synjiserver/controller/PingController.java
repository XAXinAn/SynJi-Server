package org.example.synjiserver.controller;

import org.example.synjiserver.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PingController {

    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.success("Server is running!", "Pong");
    }
}
