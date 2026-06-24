package com.freightflow.modules.document;

import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.User;
import com.freightflow.modules.shipment.Shipment;
import com.freightflow.modules.voyage.Voyage;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapping the {@code documents} table.
 *
 * <p>Stores metadata about a file uploaded to Cloudflare R2 (or mock storage
 * in dev). The actual binary content lives in object storage — only the
 * {@link #storageKey} is persisted here.</p>
 *
 * <p>Deletion is soft: {@link #active} is set to {@code false} so the
 * record remains for audit trail while the object can be removed from storage.</p>
 */
@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voyage_id")
    private Voyage voyage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DocumentType type;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /** Object storage key, e.g. {@code {tenantId}/{shipmentId}/{uuid}-{fileName}}. */
    @Column(name = "storage_key", nullable = false, length = 500)
    private String storageKey;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    protected Document() {}

    // ==================== Getters ====================

    public UUID getId()             { return id; }
    public Tenant getTenant()       { return tenant; }
    public Shipment getShipment()   { return shipment; }
    public Voyage getVoyage()       { return voyage; }
    public DocumentType getType()   { return type; }
    public String getFileName()     { return fileName; }
    public String getStorageKey()   { return storageKey; }
    public String getContentType()  { return contentType; }
    public Long getSizeBytes()      { return sizeBytes; }
    public User getUploadedBy()     { return uploadedBy; }
    public Instant getUploadedAt()  { return uploadedAt; }
    public String getDescription()  { return description; }
    public boolean isActive()       { return active; }

    // ==================== Setters ====================

    public void setTenant(Tenant tenant)             { this.tenant = tenant; }
    public void setShipment(Shipment shipment)       { this.shipment = shipment; }
    public void setVoyage(Voyage voyage)             { this.voyage = voyage; }
    public void setType(DocumentType type)           { this.type = type; }
    public void setFileName(String fileName)         { this.fileName = fileName; }
    public void setStorageKey(String storageKey)     { this.storageKey = storageKey; }
    public void setContentType(String contentType)   { this.contentType = contentType; }
    public void setSizeBytes(Long sizeBytes)         { this.sizeBytes = sizeBytes; }
    public void setUploadedBy(User uploadedBy)       { this.uploadedBy = uploadedBy; }
    public void setUploadedAt(Instant uploadedAt)    { this.uploadedAt = uploadedAt; }
    public void setDescription(String description)   { this.description = description; }
    public void setActive(boolean active)            { this.active = active; }
}
