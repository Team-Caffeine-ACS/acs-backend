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
    if (dbData == null) {
      return null;
    }
    if (LEGACY_PRE_REGISTERED.equalsIgnoreCase(dbData.trim())) {
      return VisitStatus.PLANNED;
    }
    return VisitStatus.valueOf(dbData.trim().toUpperCase());
  }
}
