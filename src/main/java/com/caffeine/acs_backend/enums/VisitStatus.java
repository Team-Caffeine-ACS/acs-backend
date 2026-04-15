package com.caffeine.acs_backend.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum VisitStatus {
  /** Visitor submitted intent to visit; not yet arrived */
  PRE_REGISTERED("Ootel"),

  /** Visitor has arrived; keycard issued */
  ACTIVE("Sees"),

  /** Visitor has left; keycard released, exit time recorded */
  COMPLETED("Väljas"),

  /** Visit was cancelled before arrival */
  CANCELLED("Tühistatud");

  private final String label;

  VisitStatus(String label) {
    this.label = label;
  }

  @JsonValue
  public String getLabel() {
    return label;
  }
}
