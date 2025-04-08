package com.project.cicd_auth.controller;

import com.project.cicd_auth.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
@RequiredArgsConstructor
public class MainController {
    private final RedisUtil redisUtil;

    @GetMapping("/")
    public String mainP() {
        return "main Controller";
    }
}
