package com.example.research2.SpringBoot.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainPageController {

    @GetMapping("/mainPage")
    public String showMainPage() {
        return "mainPage";
    }
}
