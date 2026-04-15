package com.caffeine.acs_backend.service;

import com.caffeine.acs_backend.dto.preregistration.PreRegistrationResponse;
import com.caffeine.acs_backend.dto.preregistration.UpdatePreRegistrationRequest;
import com.caffeine.acs_backend.dto.preregistration.UpdateScheduledVisitorRequest;
import com.caffeine.acs_backend.dto.visitor.CreateScheduledVisitorRequest;
import com.caffeine.acs_backend.dto.visitor.ScheduledVisitorResponse;
import com.caffeine.acs_backend.enums.VisitStatus;
import com.caffeine.acs_backend.repository.VisitRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ScheduledVisitorService {

  private final PreRegistrationService preRegistrationService;
  private final VisitRepository visitRepository;

  public Page<ScheduledVisitorResponse> getAll(
      LocalDate date, String search, VisitStatus status, UUID buildingId, Pageable pageable) {

    return preRegistrationService
        .getAll(date, search, status, buildingId, pageable)
        .map(ScheduledVisitorResponse::from);
  }

  public ScheduledVisitorResponse getById(UUID id) {
    PreRegistrationResponse response = preRegistrationService.getById(id);
    return ScheduledVisitorResponse.from(response);
  }

  public UUID create(CreateScheduledVisitorRequest request) {
    return preRegistrationService.create(request.toPreRegistrationRequest()).preRegistrationId();
  }

  public void update(UUID id, UpdateScheduledVisitorRequest request) {

    preRegistrationService.update(
        id,
        new UpdatePreRegistrationRequest(
            request.getExpectedArrival(),
            request.getHostId(),
            request.getBuildingId(),
            request.getStatus()));
  }

  public void updateStatus(UUID id, VisitStatus status) {
    preRegistrationService.update(id, new UpdatePreRegistrationRequest(null, null, null, status));
  }

  public Map<String, Long> getStats() {

    LocalDateTime todayStart = LocalDate.now().atStartOfDay();
    LocalDateTime tomorrow = todayStart.plusDays(1);

    long todayVisitors = visitRepository.countByArrivalTimeBetween(todayStart, tomorrow);

    long activeVisits = visitRepository.countByStatus(VisitStatus.ACTIVE);

    long cancelled = visitRepository.countByStatus(VisitStatus.CANCELLED);

    return Map.of(
        "todayVisitors", todayVisitors,
        "activeVisits", activeVisits,
        "issuedCards", activeVisits,
        "deniedEntries", cancelled);
  }

  public byte[] export(LocalDate date, String search, VisitStatus status, UUID buildingId) {

    List<ScheduledVisitorResponse> data =
        getAll(date, search, status, buildingId, Pageable.unpaged()).getContent();

    try (Workbook workbook = new XSSFWorkbook()) {

      Sheet sheet = workbook.createSheet("Scheduled Visitors");

      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue("ID");
      header.createCell(1).setCellValue("Full Name");
      header.createCell(2).setCellValue("Scheduled Time");
      header.createCell(3).setCellValue("Status");
      header.createCell(4).setCellValue("Host");

      int rowIdx = 1;

      for (ScheduledVisitorResponse r : data) {
        Row row = sheet.createRow(rowIdx++);

        row.createCell(0).setCellValue(String.valueOf(r.visitorId()));
        row.createCell(1).setCellValue(r.fullName());
        row.createCell(2).setCellValue(String.valueOf(r.scheduledTime()));
        row.createCell(3).setCellValue(String.valueOf(r.status()));
        row.createCell(4).setCellValue(r.hostName());
      }

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      workbook.write(out);

      return out.toByteArray();

    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Export failed");
    }
  }
}
