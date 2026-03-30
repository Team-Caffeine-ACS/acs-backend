package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.keycard.KeycardResponse;
import com.caffeine.acs_backend.repository.KeycardRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeycardService {

  private final KeycardRepository keycardRepository;

  public List<KeycardResponse> getAvailableKeycards() {
    return keycardRepository.findAvailableKeycards().stream()
        .map(KeycardResponse::from)
        .toList();
  }
}
