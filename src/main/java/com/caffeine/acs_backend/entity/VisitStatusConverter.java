package com.caffeine.acs_backend.entity;

import com.caffeine.acs_backend.enums.VisitStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class VisitStatusConverter implements AttributeConverter<VisitStatus, String> {

  private static final String LEGACY_PRE_REGISTERED = "PRE_REGISTERED";

  @Override
  public String convertToDatabaseColumn(VisitStatus attribute) {
    return attribute == null ? null : attribute.name();
  }

  @Override
  public VisitStatus convertToEntityAttribute(String dbData) {
    return parseDatabaseValue(dbData);
  }

  public static VisitStatus parseDatabaseValue(String dbData) {
    if (dbData == null || dbData.isBlank()) {
      return null;
    }

    String normalizedData = dbData.trim().toUpperCase();

    if (LEGACY_PRE_REGISTERED.equals(normalizedData)) {
      return VisitStatus.PLANNED;
    }

    try {
      return VisitStatus.valueOf(normalizedData);
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException(
          "Unknown VisitStatus found in database: " + normalizedData, e);
    }
  }
}
