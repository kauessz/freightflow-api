package com.freightflow.modules.csvimport.dto;

import com.freightflow.modules.shipment.enums.ContainerType;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa uma linha parseada do CSV de importacao de shipments.
 * Executa validacao sintatica antes de ir para o service.
 */
public class CsvShipmentRow {

    private final int rowNumber;
    private final String booking;
    private final String containerNumber;
    private final String containerType;
    private final String voyageNumber;
    private final String originUnlocode;
    private final String destinationUnlocode;
    private final String shipper;
    private final String consignee;
    private final List<String> validationErrors = new ArrayList<>();

    public CsvShipmentRow(int rowNumber, String[] columns) {
        this.rowNumber = rowNumber;

        // Parse com trim, tratando colunas faltantes
        this.booking = safeGet(columns, 0);
        this.containerNumber = safeGet(columns, 1);
        this.containerType = safeGet(columns, 2);
        this.voyageNumber = safeGet(columns, 3);
        this.originUnlocode = safeGet(columns, 4);
        this.destinationUnlocode = safeGet(columns, 5);
        this.shipper = safeGet(columns, 6);
        this.consignee = safeGet(columns, 7);

        validate();
    }

    private void validate() {
        if (booking == null || booking.isBlank()) {
            validationErrors.add("booking is required");
        } else if (!booking.matches("^[A-Z]\\d{8,10}$")) {
            validationErrors.add("booking must match pattern: letter + 8-10 digits (e.g., A123456789)");
        }

        if (containerNumber != null && !containerNumber.isBlank()
                && !containerNumber.matches("^[A-Z]{4}\\d{7}$")) {
            validationErrors.add("containerNumber must match ISO 6346: 4 letters + 7 digits (e.g., MSCU1234567)");
        }

        if (containerType != null && !containerType.isBlank()) {
            try {
                ContainerType.valueOf(containerType);
            } catch (IllegalArgumentException e) {
                validationErrors.add("containerType must be one of: TEU20, TEU40, TEU40HC, REEFER20, REEFER40");
            }
        }

        if (voyageNumber == null || voyageNumber.isBlank()) {
            validationErrors.add("voyageNumber is required");
        }

        if (originUnlocode == null || originUnlocode.isBlank()) {
            validationErrors.add("originUnlocode is required");
        } else if (!originUnlocode.matches("^[A-Z]{5}$")) {
            validationErrors.add("originUnlocode must be 5 uppercase letters (e.g., BRSSZ)");
        }

        if (destinationUnlocode == null || destinationUnlocode.isBlank()) {
            validationErrors.add("destinationUnlocode is required");
        } else if (!destinationUnlocode.matches("^[A-Z]{5}$")) {
            validationErrors.add("destinationUnlocode must be 5 uppercase letters (e.g., NLRTM)");
        }

        if (originUnlocode != null && destinationUnlocode != null
                && originUnlocode.equals(destinationUnlocode)) {
            validationErrors.add("originUnlocode and destinationUnlocode must be different");
        }
    }

    private String safeGet(String[] columns, int index) {
        if (index >= columns.length) return null;
        String value = columns[index].trim();
        // Remove aspas duplas do CSV
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value.isEmpty() ? null : value;
    }

    public boolean isValid() {
        return validationErrors.isEmpty();
    }

    public ContainerType parsedContainerType() {
        if (containerType == null || containerType.isBlank()) return null;
        return ContainerType.valueOf(containerType);
    }

    // ==================== Getters ====================

    public int getRowNumber() { return rowNumber; }
    public String getBooking() { return booking; }
    public String getContainerNumber() { return containerNumber; }
    public String getContainerType() { return containerType; }
    public String getVoyageNumber() { return voyageNumber; }
    public String getOriginUnlocode() { return originUnlocode; }
    public String getDestinationUnlocode() { return destinationUnlocode; }
    public String getShipper() { return shipper; }
    public String getConsignee() { return consignee; }
    public List<String> getValidationErrors() { return validationErrors; }
}
