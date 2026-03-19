package com.caffeine.acs_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public final class Application {

  private Application() {}

  /**
   * Entry point for the ACS backend Spring Boot application.
   *
   * @param args command-line arguments passed to the application
   */
  public static void main(final String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
