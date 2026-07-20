package com.freightflow.modules.commercial.rfq;

import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.User;
import com.freightflow.modules.commercial.rfq.enums.RfqDirection;
import com.freightflow.modules.commercial.rfq.enums.RfqServiceType;
import com.freightflow.modules.commercial.rfq.enums.RfqStatus;
import com.freightflow.modules.commercial.rfq.enums.RfqTransportMode;
import com.freightflow.modules.commercial.shared.IncotermCode;
import com.freightflow.modules.customer.Customer;
import com.freightflow.modules.port.Port;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "commercial_rfqs")
public class RequestForQuotation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 80)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(length = 255)
    private String prospectCompanyName;

    @Column(nullable = false)
    private String contactName;

    @Column
    private String contactEmail;

    @Column(length = 50)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RfqDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RfqTransportMode transportMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RfqServiceType serviceType;

    @Enumerated(EnumType.STRING)
    private IncotermCode incotermCode;

    @Column(length = 10)
    private String incotermVersion;

    @Column(length = 255)
    private String incotermNamedPlace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_port_id", nullable = false)
    private Port originPort;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_port_id", nullable = false)
    private Port destinationPort;

    @Column(length = 255)
    private String placeOfReceipt;

    @Column(length = 255)
    private String placeOfDelivery;

    @Column
    private Instant cargoReadyDate;

    @Column
    private Instant desiredDepartureDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RfqStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column
    private Instant submittedAt;

    @Column
    private Instant cancelledAt;

    @OneToMany(mappedBy = "rfq", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RfqCargoItem> cargoItems = new ArrayList<>();

    @OneToMany(mappedBy = "rfq", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RfqContainerRequirement> containerRequirements = new ArrayList<>();

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected RequestForQuotation() {
    }

    public RequestForQuotation(Tenant tenant, String reference, String contactName,
                               RfqDirection direction, RfqTransportMode transportMode,
                               RfqServiceType serviceType, Port originPort, Port destinationPort,
                               User createdBy) {
        this.tenant = tenant;
        this.reference = reference;
        this.contactName = contactName;
        this.direction = direction;
        this.transportMode = transportMode;
        this.serviceType = serviceType;
        this.originPort = originPort;
        this.destinationPort = destinationPort;
        this.createdBy = createdBy;
        this.status = RfqStatus.DRAFT;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Tenant getTenant() { return tenant; }
    public String getReference() { return reference; }
    public Customer getCustomer() { return customer; }
    public String getProspectCompanyName() { return prospectCompanyName; }
    public String getContactName() { return contactName; }
    public String getContactEmail() { return contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public RfqDirection getDirection() { return direction; }
    public RfqTransportMode getTransportMode() { return transportMode; }
    public RfqServiceType getServiceType() { return serviceType; }
    public IncotermCode getIncotermCode() { return incotermCode; }
    public String getIncotermVersion() { return incotermVersion; }
    public String getIncotermNamedPlace() { return incotermNamedPlace; }
    public Port getOriginPort() { return originPort; }
    public Port getDestinationPort() { return destinationPort; }
    public String getPlaceOfReceipt() { return placeOfReceipt; }
    public String getPlaceOfDelivery() { return placeOfDelivery; }
    public Instant getCargoReadyDate() { return cargoReadyDate; }
    public Instant getDesiredDepartureDate() { return desiredDepartureDate; }
    public RfqStatus getStatus() { return status; }
    public User getAssignedTo() { return assignedTo; }
    public String getNotes() { return notes; }
    public User getCreatedBy() { return createdBy; }
    public Instant getSubmittedAt() { return submittedAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public List<RfqCargoItem> getCargoItems() { return cargoItems; }
    public List<RfqContainerRequirement> getContainerRequirements() { return containerRequirements; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setReference(String reference) { this.reference = reference; touch(); }
    public void setCustomer(Customer customer) { this.customer = customer; touch(); }
    public void setProspectCompanyName(String prospectCompanyName) { this.prospectCompanyName = prospectCompanyName; touch(); }
    public void setContactName(String contactName) { this.contactName = contactName; touch(); }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; touch(); }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; touch(); }
    public void setDirection(RfqDirection direction) { this.direction = direction; touch(); }
    public void setTransportMode(RfqTransportMode transportMode) { this.transportMode = transportMode; touch(); }
    public void setServiceType(RfqServiceType serviceType) { this.serviceType = serviceType; touch(); }
    public void setIncotermCode(IncotermCode incotermCode) { this.incotermCode = incotermCode; touch(); }
    public void setIncotermVersion(String incotermVersion) { this.incotermVersion = incotermVersion; touch(); }
    public void setIncotermNamedPlace(String incotermNamedPlace) { this.incotermNamedPlace = incotermNamedPlace; touch(); }
    public void setOriginPort(Port originPort) { this.originPort = originPort; touch(); }
    public void setDestinationPort(Port destinationPort) { this.destinationPort = destinationPort; touch(); }
    public void setPlaceOfReceipt(String placeOfReceipt) { this.placeOfReceipt = placeOfReceipt; touch(); }
    public void setPlaceOfDelivery(String placeOfDelivery) { this.placeOfDelivery = placeOfDelivery; touch(); }
    public void setCargoReadyDate(Instant cargoReadyDate) { this.cargoReadyDate = cargoReadyDate; touch(); }
    public void setDesiredDepartureDate(Instant desiredDepartureDate) { this.desiredDepartureDate = desiredDepartureDate; touch(); }
    public void setStatus(RfqStatus status) { this.status = status; touch(); }
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; touch(); }
    public void setNotes(String notes) { this.notes = notes; touch(); }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; touch(); }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; touch(); }

    public void replaceCargoItems(List<RfqCargoItem> items) {
        this.cargoItems.clear();
        if (items != null) {
            items.forEach(this::addCargoItem);
        }
        touch();
    }

    public void replaceContainerRequirements(List<RfqContainerRequirement> items) {
        this.containerRequirements.clear();
        if (items != null) {
            items.forEach(this::addContainerRequirement);
        }
        touch();
    }

    public void addCargoItem(RfqCargoItem item) {
        item.setRfq(this);
        this.cargoItems.add(item);
        touch();
    }

    public void addContainerRequirement(RfqContainerRequirement item) {
        item.setRfq(this);
        this.containerRequirements.add(item);
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }
}
