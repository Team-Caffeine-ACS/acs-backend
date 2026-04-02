package com.caffeine.acs_backend.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("test")
@RestController
@RequestMapping("/api")
public class AdminController {

  @GetMapping("/admin")
  public String adminEndpoint() {
    return "ADMIN_OK";
  }
}
