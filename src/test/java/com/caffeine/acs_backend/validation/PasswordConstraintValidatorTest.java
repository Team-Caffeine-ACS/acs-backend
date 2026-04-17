package com.caffeine.acs_backend.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordConstraintValidatorTest {

  private final PasswordConstraintValidator validator = new PasswordConstraintValidator();

  @Test
  void nullPassword_isValid() {
    assertThat(validator.isValid(null, null)).isTrue();
  }

  @Test
  void validPassword_isValid() {
    assertThat(validator.isValid("Password1!", null)).isTrue();
  }

  @Test
  void exactlyEightCharsWithAllRequirements_isValid() {
    assertThat(validator.isValid("Passw0r!", null)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "Pass1!", // too short (6 chars)
        "Pass1!x", // too short (7 chars)
      })
  void tooShort_isInvalid(String password) {
    assertThat(validator.isValid(password, null)).isFalse();
  }

  @Test
  void noUppercase_isInvalid() {
    assertThat(validator.isValid("password1!", null)).isFalse();
  }

  @Test
  void noLowercase_isInvalid() {
    assertThat(validator.isValid("PASSWORD1!", null)).isFalse();
  }

  @Test
  void noDigit_isInvalid() {
    assertThat(validator.isValid("Password!!", null)).isFalse();
  }

  @Test
  void noSpecialCharacter_isInvalid() {
    assertThat(validator.isValid("Password1", null)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "Password1@",
        "MyP@ssw0rd",
        "C0mpl3x#Pass",
        "Str0ng!Password",
      })
  void variousValidPasswords_areValid(String password) {
    assertThat(validator.isValid(password, null)).isTrue();
  }
}
