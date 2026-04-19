package com.caffeine.acs_backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

  private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
  private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
  private static final Pattern DIGIT = Pattern.compile("\\d");
  private static final Pattern SPECIAL = Pattern.compile("[^a-zA-Z0-9]");

  @Override
  public boolean isValid(String password, ConstraintValidatorContext context) {
    if (password == null) {
      return true; // null is handled by @NotBlank where required; optional fields skip validation
    }
    return password.length() >= 8
        && UPPERCASE.matcher(password).find()
        && LOWERCASE.matcher(password).find()
        && DIGIT.matcher(password).find()
        && SPECIAL.matcher(password).find();
  }
}
