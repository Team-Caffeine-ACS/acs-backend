package com.caffeine.acs_backend.repository;

import java.time.LocalDateTime;
import java.util.UUID;

/** Spring Data projection interface for the native visit list query. */
public interface VisitListView {

  UUID getId();

  /** Full name of the visitor (given_name + surname). */
  String getFullName();

  /** Document number from the visitor's first document, null if none on record. */
  String getDocumentNumber();

  /** Organization of the visitor. */
  String getOrganizationName();

  /** Full name of the host, null when no host assigned. */
  String getHostName();

  LocalDateTime getEntryTime();

  LocalDateTime getExitTime();

  /** Computed status: in_building, departed, or expired. */
  String getVisitStatus();

  /** person.id of the visitor. */
  UUID getVisitorId();

  /** ID of the access point. */
  UUID getAccessPointId();
}
