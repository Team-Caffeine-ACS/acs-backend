package com.caffeine.acs_backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ActiveProfiles("test")
@RestController
@RequestMapping("/api/test")
public class TestSecurityController {

  @GetMapping("/protected")
  public String protectedEndpoint() {
    return "OK";
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/admin")
  public String adminEndpoint() {
    return "ADMIN OK";
  }
}
