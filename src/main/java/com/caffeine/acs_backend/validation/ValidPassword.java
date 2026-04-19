package com.caffeine.acs_backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PasswordConstraintValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

  String message() default
      "Password must be at least 8 characters and contain an uppercase letter, a lowercase letter, a number, and a special character";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
