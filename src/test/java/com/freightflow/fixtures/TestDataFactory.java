package com.freightflow.fixtures;

import com.freightflow.modules.alert.Alert;
import com.freightflow.modules.alert.enums.AlertType;
import com.freightflow.modules.alert.enums.Severity;
import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.User;
import com.freightflow.modules.event.Event;
import com.freightflow.modules.event.enums.EventType;
import com.freightflow.modules.port.Port;
import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.shipment.enums.ContainerType;
import com.freightflow.modules.vessel.Vessel;
import com.freightflow.modules.vessel.enums.VesselType;
import com.freightflow.modules.voyage.Voyage;
import com.freightflow.shared.security.UserPrincipal;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Factory para criacao de entidades de teste.
 * Usa reflection para setar IDs (campos gerados pelo JPA).
 */
public final class TestDataFactory {

    private TestDataFactory() {}

    private static final UUID DEFAULT_TENANT_ID = UUID.fromString("aaaa0000-0000-0000-0000-000000000001");
    private static final UUID DEFAULT_USER_ID = UUID.fromString("bbbb0000-0000-0000-0000-000000000001");
    private static final UUID DEFAULT_PORT_ORIGIN_ID = UUID.fromString("cccc0000-0000-0000-0000-000000000001");
    private static final UUID DEFAULT_PORT_DEST_ID = UUID.fromString("cccc0000-0000-0000-0000-000000000002");
    private static final UUID DEFAULT_VESSEL_ID = UUID.fromString("dddd0000-0000-0000-0000-000000000001");
    private static final UUID DEFAULT_VOYAGE_ID = UUID.fromString("eeee0000-0000-0000-0000-000000000001");
    private static final UUID DEFAULT_SHIPMENT_ID = UUID.fromString("ffff0000-0000-0000-0000-000000000001");

    // ==================== Tenant ====================

    public static Tenant tenant() {
        return tenant(DEFAULT_TENANT_ID, "Mercosul Line", "mercosul-line");
    }

    public static Tenant tenant(UUID id, String name, String slug) {
        Tenant tenant = new Tenant(name, slug, "contact@" + slug + ".com", "FREE");
        setId(tenant, id);
        return tenant;
    }

    // ==================== User ====================

    public static User user() {
        return user(DEFAULT_USER_ID, "Kaue", "kaue@mercosul.com", User.UserRole.ADMIN);
    }

    public static User user(UUID id, String name, String email, User.UserRole role) {
        User user = new User(name, email, "$2a$10$hashedPasswordPlaceholder", role, tenant());
        setId(user, id);
        return user;
    }

    public static User userWithTenant(UUID id, String name, String email, User.UserRole role, Tenant tenant) {
        User user = new User(name, email, "$2a$10$hashedPasswordPlaceholder", role, tenant);
        setId(user, id);
        return user;
    }

    // ==================== UserPrincipal ====================

    public static UserPrincipal principal() {
        return new UserPrincipal(DEFAULT_USER_ID, "kaue@mercosul.com", null, DEFAULT_TENANT_ID, "ADMIN", null);
    }

    public static UserPrincipal principal(UUID userId, UUID tenantId) {
        return new UserPrincipal(userId, "user@test.com", null, tenantId, "ADMIN", null);
    }

    // ==================== Port ====================

    public static Port santos() {
        Port port = new Port("BRSSZ", "Santos", "BR", "America/Sao_Paulo", -23.9536, -46.3336);
        setId(port, DEFAULT_PORT_ORIGIN_ID);
        return port;
    }

    public static Port rotterdam() {
        Port port = new Port("NLRTM", "Rotterdam", "NL", "Europe/Amsterdam", 51.9225, 4.4792);
        setId(port, DEFAULT_PORT_DEST_ID);
        return port;
    }

    public static Port port(UUID id, String unlocode, String name, String country) {
        Port port = new Port(unlocode, name, country, "UTC", 0.0, 0.0);
        setId(port, id);
        return port;
    }

    // ==================== Vessel ====================

    public static Vessel vessel() {
        return vessel(DEFAULT_VESSEL_ID, "9839012", "MSC Oscar");
    }

    public static Vessel vessel(UUID id, String imo, String name) {
        Vessel vessel = new Vessel(imo, name, "PA", VesselType.CONTAINER, 19224);
        setId(vessel, id);
        return vessel;
    }

    // ==================== Voyage ====================

    public static Voyage voyage() {
        return voyage(DEFAULT_VOYAGE_ID, "MSC-2026-001");
    }

    public static Voyage voyage(UUID id, String voyageNumber) {
        Instant etd = Instant.now().plus(3, ChronoUnit.DAYS);
        Instant eta = Instant.now().plus(21, ChronoUnit.DAYS);
        Voyage voyage = new Voyage(voyageNumber, vessel(), santos(), rotterdam(), etd, eta);
        setId(voyage, id);
        return voyage;
    }

    // ==================== Shipment ====================

    public static Shipment shipment() {
        return shipment(DEFAULT_SHIPMENT_ID, "A123456789");
    }

    public static Shipment shipment(UUID id, String booking) {
        Shipment shipment = new Shipment(booking, voyage(), santos(), rotterdam(), tenant());
        shipment.setContainerNumber("MSCU1234567");
        shipment.setContainerType(ContainerType.TEU40);
        shipment.setShipper("Brazil Exports Ltda");
        shipment.setConsignee("European Imports BV");
        setId(shipment, id);
        return shipment;
    }

    public static Shipment shipmentWithTenant(UUID id, String booking, Tenant tenant) {
        Shipment shipment = new Shipment(booking, voyage(), santos(), rotterdam(), tenant);
        shipment.setContainerNumber("MSCU1234567");
        shipment.setContainerType(ContainerType.TEU40);
        setId(shipment, id);
        return shipment;
    }

    // ==================== Event ====================

    public static Event event(Shipment shipment, EventType type) {
        return new Event(shipment, type, "Santos, BR", "Test event", Instant.now());
    }

    // ==================== Alert ====================

    public static Alert alert(Shipment shipment) {
        return new Alert(shipment, AlertType.DELAY, Severity.MEDIUM, "Vessel delayed by 12 hours");
    }

    // ==================== CSV ====================

    public static String validCsvContent() {
        return "booking,containerNumber,containerType,voyageNumber,originUnlocode,destinationUnlocode,shipper,consignee\n"
                + "A111111111,MSCU1111111,TEU40,MSC-2026-001,BRSSZ,NLRTM,Exportador BR,Importador NL\n"
                + "B222222222,CMAU2222222,TEU40HC,MSC-2026-001,BRSSZ,NLRTM,Another Shipper,Another Consignee\n";
    }

    public static String csvWithErrors() {
        return "booking,containerNumber,containerType,voyageNumber,originUnlocode,destinationUnlocode,shipper,consignee\n"
                + "AB,,TEU40,MSC-2026-001,BRSSZ,NLRTM,Shipper,Consignee\n"
                + "A333333333,MSCU3333333,INVALID_TYPE,MSC-2026-001,BRSSZ,NLRTM,Shipper,Consignee\n"
                + "A444444444,MSCU4444444,TEU40,FAKE-999,BRSSZ,NLRTM,Shipper,Consignee\n";
    }

    public static String csvWithInvalidHeader() {
        return "wrong,headers,here\ndata,data,data\n";
    }

    // ==================== Default IDs ====================

    public static UUID defaultTenantId() { return DEFAULT_TENANT_ID; }
    public static UUID defaultUserId() { return DEFAULT_USER_ID; }
    public static UUID defaultPortOriginId() { return DEFAULT_PORT_ORIGIN_ID; }
    public static UUID defaultPortDestId() { return DEFAULT_PORT_DEST_ID; }
    public static UUID defaultVesselId() { return DEFAULT_VESSEL_ID; }
    public static UUID defaultVoyageId() { return DEFAULT_VOYAGE_ID; }
    public static UUID defaultShipmentId() { return DEFAULT_SHIPMENT_ID; }

    // ==================== Reflection helper ====================

    /**
     * Sets the ID of a JPA entity via reflection.
     * Public so tests can set IDs on entities returned by mocked save().
     */
    public static void setEntityId(Object entity, UUID id) {
        setId(entity, id);
    }

    private static void setId(Object entity, UUID id) {
        try {
            Field idField = findIdField(entity.getClass());
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID on " + entity.getClass().getSimpleName(), e);
        }
    }

    private static Field findIdField(Class<?> clazz) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField("id");
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                return findIdField(clazz.getSuperclass());
            }
            throw e;
        }
    }
}
