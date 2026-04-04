package com.freightflow.modules.shipment;

import jakarta.persistence.*;
import com.freightflow.modules.voyage.Voyage;
import com.freightflow.modules.port.Port;
import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.shipment.enums.ShipmentStatus;
import com.freightflow.modules.shipment.enums.ContainerType;
import com.freightflow.modules.event.Event;
import com.freightflow.modules.event.enums.EventType;
import com.freightflow.modules.alert.Alert;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "shipments")
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String booking;

    // Documentos BL
    @Column(length = 50)
    private String houseBl;

    @Column(length = 50)
    private String masterBl;

    @Column(length = 50)
    private String customerReference;

    // Container
    @Column(length = 11)
    private String containerNumber;

    @Column
    @Enumerated(EnumType.STRING)
    private ContainerType containerType;

    @Column
    private Integer containerSizeFt;

    @Column(length = 10)
    private String containerIsoCode;

    @Column(precision = 12, scale = 2)
    private BigDecimal grossWeightKg;

    @Column(precision = 12, scale = 2)
    private BigDecimal netWeightKg;

    @Column(precision = 10, scale = 2)
    private BigDecimal volumeCbm;

    @Column
    private Integer packages;

    @Column(length = 50)
    private String packageType;

    // Relações
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voyage_id", nullable = false)
    private Voyage voyage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_port_id", nullable = false)
    private Port originPort;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_port_id", nullable = false)
    private Port destinationPort;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transshipment_port_id")
    private Port transshipmentPort;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private com.freightflow.modules.customer.Customer customer;

    // Partes
    @Column
    private String consignee;

    @Column
    private String shipper;

    @Column(length = 255)
    private String notifyParty;

    @Column(length = 100)
    private String operatorName;

    // Dados operacionais
    @Column(length = 10)
    private String incoterm;

    @Column(length = 20)
    private String freightTerm;

    @Column(length = 255)
    private String cargoDescription;

    @Column(length = 100)
    private String serviceLane;

    // Status operacionais
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

    @Column(length = 30)
    private String documentStatus = "PENDING";

    @Column(length = 30)
    private String customsStatus = "NOT_STARTED";

    @Column(length = 20)
    private String riskLevel = "LOW";

    @Column
    private Integer delayDays = 0;

    // AIS / notas
    @Column(length = 500)
    private String vesselSourceUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("occurredAt DESC")
    private List<Event> events = new ArrayList<>();

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alert> alerts = new ArrayList<>();

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Shipment() {}

    public Shipment(String booking, Voyage voyage, Port originPort, Port destinationPort, Tenant tenant) {
        this.booking = booking;
        this.voyage = voyage;
        this.originPort = originPort;
        this.destinationPort = destinationPort;
        this.tenant = tenant;
        this.status = ShipmentStatus.BOOKED;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void addEvent(Event event) {
        this.events.add(event);
        updateStatusFromEvent(event);
        this.updatedAt = Instant.now();
    }

    private void updateStatusFromEvent(Event event) {
        switch (event.getType()) {
            case GATE_IN -> this.status = ShipmentStatus.GATE_IN;
            case LOADED -> this.status = ShipmentStatus.LOADED;
            case DEPARTED -> this.status = ShipmentStatus.IN_TRANSIT;
            case ARRIVED -> this.status = ShipmentStatus.ARRIVED;
            case GATE_OUT -> this.status = ShipmentStatus.GATE_OUT;
            case CUSTOMS_RELEASE -> {
                if (this.status == ShipmentStatus.ARRIVED) {
                    this.status = ShipmentStatus.DELIVERED;
                }
            }
            default -> {}
        }
    }

    public Event lastEvent() {
        return events.isEmpty() ? null : events.get(0);
    }

    public boolean hasUnresolvedAlerts() {
        return alerts.stream().anyMatch(alert -> !alert.isResolved());
    }

    // ==================== Getters ====================

    public UUID getId() { return id; }
    public String getBooking() { return booking; }
    public String getHouseBl() { return houseBl; }
    public String getMasterBl() { return masterBl; }
    public String getCustomerReference() { return customerReference; }
    public String getContainerNumber() { return containerNumber; }
    public ContainerType getContainerType() { return containerType; }
    public Integer getContainerSizeFt() { return containerSizeFt; }
    public String getContainerIsoCode() { return containerIsoCode; }
    public BigDecimal getGrossWeightKg() { return grossWeightKg; }
    public BigDecimal getNetWeightKg() { return netWeightKg; }
    public BigDecimal getVolumeCbm() { return volumeCbm; }
    public Integer getPackages() { return packages; }
    public String getPackageType() { return packageType; }
    public Voyage getVoyage() { return voyage; }
    public Port getOriginPort() { return originPort; }
    public Port getDestinationPort() { return destinationPort; }
    public Port getTransshipmentPort() { return transshipmentPort; }
    public Tenant getTenant() { return tenant; }
    public com.freightflow.modules.customer.Customer getCustomer() { return customer; }
    public String getConsignee() { return consignee; }
    public String getShipper() { return shipper; }
    public String getNotifyParty() { return notifyParty; }
    public String getOperatorName() { return operatorName; }
    public String getIncoterm() { return incoterm; }
    public String getFreightTerm() { return freightTerm; }
    public String getCargoDescription() { return cargoDescription; }
    public String getServiceLane() { return serviceLane; }
    public ShipmentStatus getStatus() { return status; }
    public String getDocumentStatus() { return documentStatus; }
    public String getCustomsStatus() { return customsStatus; }
    public String getRiskLevel() { return riskLevel; }
    public Integer getDelayDays() { return delayDays; }
    public String getVesselSourceUrl() { return vesselSourceUrl; }
    public String getNotes() { return notes; }
    public List<Event> getEvents() { return events; }
    public List<Alert> getAlerts() { return alerts; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // ==================== Setters ====================

    public void setContainerNumber(String containerNumber) {
        this.containerNumber = containerNumber;
        this.updatedAt = Instant.now();
    }

    public void setContainerType(ContainerType containerType) {
        this.containerType = containerType;
        this.updatedAt = Instant.now();
    }

    public void setConsignee(String consignee) {
        this.consignee = consignee;
        this.updatedAt = Instant.now();
    }

    public void setShipper(String shipper) {
        this.shipper = shipper;
        this.updatedAt = Instant.now();
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public void setHouseBl(String houseBl) { this.houseBl = houseBl; this.updatedAt = Instant.now(); }
    public void setMasterBl(String masterBl) { this.masterBl = masterBl; this.updatedAt = Instant.now(); }
    public void setCustomerReference(String customerReference) { this.customerReference = customerReference; this.updatedAt = Instant.now(); }
    public void setNotifyParty(String notifyParty) { this.notifyParty = notifyParty; this.updatedAt = Instant.now(); }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; this.updatedAt = Instant.now(); }
    public void setIncoterm(String incoterm) { this.incoterm = incoterm; this.updatedAt = Instant.now(); }
    public void setFreightTerm(String freightTerm) { this.freightTerm = freightTerm; this.updatedAt = Instant.now(); }
    public void setCargoDescription(String cargoDescription) { this.cargoDescription = cargoDescription; this.updatedAt = Instant.now(); }
    public void setServiceLane(String serviceLane) { this.serviceLane = serviceLane; this.updatedAt = Instant.now(); }
    public void setDocumentStatus(String documentStatus) { this.documentStatus = documentStatus; this.updatedAt = Instant.now(); }
    public void setCustomsStatus(String customsStatus) { this.customsStatus = customsStatus; this.updatedAt = Instant.now(); }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; this.updatedAt = Instant.now(); }
    public void setDelayDays(Integer delayDays) { this.delayDays = delayDays; this.updatedAt = Instant.now(); }
    public void setVesselSourceUrl(String vesselSourceUrl) { this.vesselSourceUrl = vesselSourceUrl; this.updatedAt = Instant.now(); }
    public void setNotes(String notes) { this.notes = notes; this.updatedAt = Instant.now(); }
    public void setContainerSizeFt(Integer containerSizeFt) { this.containerSizeFt = containerSizeFt; this.updatedAt = Instant.now(); }
    public void setContainerIsoCode(String containerIsoCode) { this.containerIsoCode = containerIsoCode; this.updatedAt = Instant.now(); }
    public void setGrossWeightKg(BigDecimal grossWeightKg) { this.grossWeightKg = grossWeightKg; this.updatedAt = Instant.now(); }
    public void setNetWeightKg(BigDecimal netWeightKg) { this.netWeightKg = netWeightKg; this.updatedAt = Instant.now(); }
    public void setVolumeCbm(BigDecimal volumeCbm) { this.volumeCbm = volumeCbm; this.updatedAt = Instant.now(); }
    public void setPackages(Integer packages) { this.packages = packages; this.updatedAt = Instant.now(); }
    public void setPackageType(String packageType) { this.packageType = packageType; this.updatedAt = Instant.now(); }
}
