package com.example.research2.SpringBoot.controllers;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("name")
    public String globalUsername(Principal principal) {
        return principal != null ? principal.getName() : null;
    }
}

