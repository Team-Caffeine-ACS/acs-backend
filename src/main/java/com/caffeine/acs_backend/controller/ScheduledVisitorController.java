package com.caffeine.acs_backend.controller;

import com.caffeine.acs_backend.dto.preregistration.UpdateScheduledVisitorRequest;
import com.caffeine.acs_backend.dto.visitor.CreateScheduledVisitorRequest;
import com.caffeine.acs_backend.dto.visitor.ScheduledVisitorResponse;
import com.caffeine.acs_backend.dto.visitor.UpdateStatusRequest;
import com.caffeine.acs_backend.enums.VisitStatus;
import com.caffeine.acs_backend.service.ScheduledVisitorService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scheduled-visitors")
@RequiredArgsConstructor
public class ScheduledVisitorController {

  private final ScheduledVisitorService service;

  @GetMapping
  public Page<ScheduledVisitorResponse> getAll(
      @RequestParam(required = false) LocalDate date,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) VisitStatus status,
      @RequestParam(required = false) UUID buildingId,
      Pageable pageable) {
    return service.getAll(date, search, status, buildingId, pageable);
  }

  @GetMapping("/{id}")
  public ScheduledVisitorResponse getById(@PathVariable UUID id) {
    return service.getById(id);
  }

  @PostMapping
  public UUID create(@RequestBody @Valid CreateScheduledVisitorRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}/status")
  public void updateStatus(@PathVariable UUID id, @RequestBody @Valid UpdateStatusRequest request) {
    service.updateStatus(id, request.getStatus());
  }

  @GetMapping("/stats")
  public Map<String, Long> stats() {
    return service.getStats();
  }

  @PutMapping("/{id}")
  public void update(
      @PathVariable UUID id, @RequestBody @Valid UpdateScheduledVisitorRequest request) {
    service.update(id, request);
  }

  @GetMapping("/export")
  public ResponseEntity<byte[]> export(
      @RequestParam(required = false) LocalDate date,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) VisitStatus status,
      @RequestParam(required = false) UUID buildingId) {

    byte[] file = service.export(date, search, status, buildingId);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=scheduled-visitors.xlsx")
        .header(
            HttpHeaders.CONTENT_TYPE,
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        .body(file);
  }
}
