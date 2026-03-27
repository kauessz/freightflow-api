package com.freightflow.modules.csvimport;

import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.csvimport.dto.CsvShipmentRow;
import com.freightflow.modules.csvimport.dto.ImportResult;
import com.freightflow.modules.port.Port;
import com.freightflow.modules.port.PortRepository;
import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.shipment.repository.ShipmentRepository;
import com.freightflow.modules.voyage.Voyage;
import com.freightflow.modules.voyage.VoyageRepository;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ImportService {

    private static final Logger log = LoggerFactory.getLogger(ImportService.class);

    private static final int MAX_ROWS = 500;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String EXPECTED_HEADER = "booking,containerNumber,containerType,voyageNumber,originUnlocode,destinationUnlocode,shipper,consignee";

    private final ShipmentRepository shipmentRepository;
    private final VoyageRepository voyageRepository;
    private final PortRepository portRepository;
    private final TenantRepository tenantRepository;

    public ImportService(ShipmentRepository shipmentRepository,
                         VoyageRepository voyageRepository,
                         PortRepository portRepository,
                         TenantRepository tenantRepository) {
        this.shipmentRepository = shipmentRepository;
        this.voyageRepository = voyageRepository;
        this.portRepository = portRepository;
        this.tenantRepository = tenantRepository;
    }

    // ==================== Import ====================

    @Transactional
    public ImportResult importShipments(MultipartFile file, UUID tenantId) {
        log.info("Starting CSV import for tenant={}, file={}, size={}",
                tenantId, file.getOriginalFilename(), file.getSize());

        validateFile(file);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        List<CsvShipmentRow> rows = parseFile(file);

        if (rows.isEmpty()) {
            throw new BusinessException("CSV file has no data rows");
        }

        if (rows.size() > MAX_ROWS) {
            throw new BusinessException("CSV file exceeds maximum of " + MAX_ROWS + " rows. Found: " + rows.size());
        }

        List<ImportResult.ImportedShipment> imported = new ArrayList<>();
        List<ImportResult.ImportError> errors = new ArrayList<>();

        for (CsvShipmentRow row : rows) {
            try {
                if (!row.isValid()) {
                    errors.add(new ImportResult.ImportError(
                            row.getRowNumber(), row.getBooking(), row.getValidationErrors()));
                    continue;
                }

                processRow(row, tenant, imported, errors);
            } catch (Exception e) {
                log.warn("Unexpected error processing row {}: {}", row.getRowNumber(), e.getMessage());
                errors.add(new ImportResult.ImportError(
                        row.getRowNumber(), row.getBooking(), List.of("Unexpected error: " + e.getMessage())));
            }
        }

        log.info("CSV import completed: total={}, success={}, errors={}",
                rows.size(), imported.size(), errors.size());

        return ImportResult.of(imported, errors, rows.size());
    }

    // ==================== Template ====================

    public String generateTemplate() {
        return EXPECTED_HEADER + "\n"
                + "A123456789,MSCU1234567,TEU40,MSC-2026-001,BRSSZ,NLRTM,Brazil Exports Ltda,European Imports BV\n"
                + "B987654321,CMAU7654321,TEU40HC,CMA-2026-042,NLRTM,CNSHA,Dutch Trading Co,Shanghai Logistics\n"
                + "C111222333,MAEU9876543,REEFER40,MAE-2026-088,SGSIN,BRSSZ,Singapore Fresh Ltd,Importadora Santos\n";
    }

    public String getFormatDocumentation() {
        return """
                {
                  "format": "CSV (Comma-Separated Values)",
                  "encoding": "UTF-8",
                  "maxRows": 500,
                  "maxFileSize": "5MB",
                  "header": "booking,containerNumber,containerType,voyageNumber,originUnlocode,destinationUnlocode,shipper,consignee",
                  "columns": [
                    {"name": "booking", "required": true, "pattern": "Letter + 8-10 digits", "example": "A123456789"},
                    {"name": "containerNumber", "required": false, "pattern": "4 letters + 7 digits (ISO 6346)", "example": "MSCU1234567"},
                    {"name": "containerType", "required": false, "values": ["TEU20", "TEU40", "TEU40HC", "REEFER20", "REEFER40"]},
                    {"name": "voyageNumber", "required": true, "description": "Must match an existing voyage", "example": "MSC-2026-001"},
                    {"name": "originUnlocode", "required": true, "pattern": "5 uppercase letters", "example": "BRSSZ"},
                    {"name": "destinationUnlocode", "required": true, "pattern": "5 uppercase letters", "example": "NLRTM"},
                    {"name": "shipper", "required": false, "maxLength": 200},
                    {"name": "consignee", "required": false, "maxLength": 200}
                  ],
                  "notes": [
                    "First row must be the header row",
                    "Booking must be unique per tenant",
                    "Voyage must already exist in the system",
                    "Ports are looked up by UNLOCODE and must exist in the system",
                    "Empty optional fields should be left blank (not null)"
                  ]
                }
                """;
    }

    // ==================== Private helpers ====================

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("File exceeds maximum size of 5MB. Size: "
                    + (file.getSize() / 1024) + "KB");
        }

        String filename = file.getOriginalFilename();
        if (filename != null && !filename.toLowerCase().endsWith(".csv")) {
            throw new BusinessException("Only .csv files are accepted. Received: " + filename);
        }
    }

    private List<CsvShipmentRow> parseFile(MultipartFile file) {
        List<CsvShipmentRow> rows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new BusinessException("CSV file is empty");
            }

            // Valida header (normaliza espacos e case)
            String normalizedHeader = headerLine.trim().toLowerCase().replaceAll("\\s+", "");
            String expectedNormalized = EXPECTED_HEADER.toLowerCase().replaceAll("\\s+", "");
            if (!normalizedHeader.equals(expectedNormalized)) {
                throw new BusinessException("Invalid CSV header. Expected: " + EXPECTED_HEADER
                        + " — Received: " + headerLine.trim());
            }

            int rowNumber = 1;
            String line;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue; // Pula linhas em branco

                String[] columns = trimmed.split(",", -1); // -1 para manter campos vazios no final
                rows.add(new CsvShipmentRow(rowNumber, columns));
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Failed to parse CSV file: " + e.getMessage());
        }

        return rows;
    }

    private void processRow(CsvShipmentRow row, Tenant tenant,
                            List<ImportResult.ImportedShipment> imported,
                            List<ImportResult.ImportError> errors) {

        List<String> rowErrors = new ArrayList<>();

        // Verificar booking duplicado no tenant
        if (shipmentRepository.existsByBookingAndTenantId(row.getBooking(), tenant.getId())) {
            rowErrors.add("Booking " + row.getBooking() + " already exists for this tenant");
        }

        // Verificar booking duplicado global
        if (shipmentRepository.existsByBooking(row.getBooking())) {
            rowErrors.add("Booking " + row.getBooking() + " already exists in the system");
        }

        // Buscar voyage por voyageNumber
        Voyage voyage = voyageRepository.findByVoyageNumber(row.getVoyageNumber()).orElse(null);
        if (voyage == null) {
            rowErrors.add("Voyage not found: " + row.getVoyageNumber());
        }

        // Buscar ports por UNLOCODE
        Port originPort = portRepository.findByUnlocode(row.getOriginUnlocode()).orElse(null);
        if (originPort == null) {
            rowErrors.add("Origin port not found: " + row.getOriginUnlocode());
        }

        Port destinationPort = portRepository.findByUnlocode(row.getDestinationUnlocode()).orElse(null);
        if (destinationPort == null) {
            rowErrors.add("Destination port not found: " + row.getDestinationUnlocode());
        }

        if (!rowErrors.isEmpty()) {
            errors.add(new ImportResult.ImportError(row.getRowNumber(), row.getBooking(), rowErrors));
            return;
        }

        // Criar shipment
        Shipment shipment = new Shipment(row.getBooking(), voyage, originPort, destinationPort, tenant);
        shipment.setContainerNumber(row.getContainerNumber());
        shipment.setContainerType(row.parsedContainerType());
        shipment.setShipper(row.getShipper());
        shipment.setConsignee(row.getConsignee());

        Shipment saved = shipmentRepository.save(shipment);

        imported.add(new ImportResult.ImportedShipment(
                row.getRowNumber(),
                saved.getId(),
                saved.getBooking(),
                saved.getContainerNumber(),
                row.getVoyageNumber(),
                row.getOriginUnlocode(),
                row.getDestinationUnlocode(),
                saved.getStatus().name()
        ));
    }
}
