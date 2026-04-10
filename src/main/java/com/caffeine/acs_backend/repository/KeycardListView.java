package com.caffeine.acs_backend.repository;

import java.time.LocalDateTime;
import java.util.UUID;

/** Spring Data projection interface for the native keycard list query. */
public interface KeycardListView {

  UUID getId();

  String getKeycardNumber();

  /** Computed status: AVAILABLE, IN_USE, DISABLED, or EXPIRED. */
  String getStatus();

  /** Full name of the current holder, null when card is not in use. */
  String getAssignedUser();

  /** Most recent return time across all possessions, null if never returned. */
  LocalDateTime getLastReturnTime();
}
