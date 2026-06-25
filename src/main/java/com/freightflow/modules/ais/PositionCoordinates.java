package com.freightflow.modules.ais;

import java.util.Optional;

public final class PositionCoordinates {

    private PositionCoordinates() {
    }

    public static Optional<CoordinatePair> parseProviderCoordinates(Object latitude, Object longitude) {
        Double lat = parseCoordinateValue(latitude, true);
        Double lon = parseCoordinateValue(longitude, true);
        if (!isValid(lat, lon)) {
            return Optional.empty();
        }
        return Optional.of(new CoordinatePair(lat, lon));
    }

    public static Optional<CoordinatePair> parseStoredLocation(String location) {
        if (location == null || location.isBlank()) {
            return Optional.empty();
        }

        String[] parts = location.split(",", -1);
        if (parts.length != 2) {
            return Optional.empty();
        }

        Double lat = parseCoordinateValue(parts[0], false);
        Double lon = parseCoordinateValue(parts[1], false);
        if (!isValid(lat, lon)) {
            return Optional.empty();
        }

        return Optional.of(new CoordinatePair(lat, lon));
    }

    public static boolean isValid(Double latitude, Double longitude) {
        return isValidLatitude(latitude) && isValidLongitude(longitude);
    }

    public static boolean isValidLatitude(Double latitude) {
        return latitude != null && latitude >= -90.0 && latitude <= 90.0;
    }

    public static boolean isValidLongitude(Double longitude) {
        return longitude != null && longitude >= -180.0 && longitude <= 180.0;
    }

    private static Double parseCoordinateValue(Object value, boolean allowDecimalComma) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }

        String normalized = String.valueOf(value).trim();
        if (normalized.isEmpty()) {
            return null;
        }

        if (allowDecimalComma && normalized.indexOf(',') >= 0 && normalized.indexOf('.') < 0) {
            if (normalized.indexOf(',') != normalized.lastIndexOf(',')) {
                return null;
            }
            normalized = normalized.replace(',', '.');
        }

        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public record CoordinatePair(double latitude, double longitude) {
    }
}
