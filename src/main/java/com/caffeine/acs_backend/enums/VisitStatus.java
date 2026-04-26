package com.caffeine.acs_backend.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum VisitStatus {
  /** Visitor submitted intent to visit; not yet arrived */
  PLANNED("Ootel"),

  /** Visitor has arrived; keycard issued */
  IN_BUILDING("Hoones"),

  /** Visitor has left; keycard released, exit time recorded */
  DEPARTED("Lahkunud"),

  /** Visit was cancelled before arrival */
  EXPIRED("Aegunud");

  private final String label;

  VisitStatus(String label) {
    this.label = label;
  }

  @JsonValue
  public String getLabel() {
    return label;
  }
}
