package com.freightflow.modules.commercial;

import com.freightflow.AbstractIntegrationTest;
import com.freightflow.modules.auth.Tenant;
import com.freightflow.modules.auth.TenantRepository;
import com.freightflow.modules.auth.User;
import com.freightflow.modules.auth.UserRepository;
import com.freightflow.modules.commercial.quotation.Quotation;
import com.freightflow.modules.commercial.quotation.QuotationRepository;
import com.freightflow.modules.commercial.rfq.RequestForQuotation;
import com.freightflow.modules.commercial.rfq.RfqRepository;
import com.freightflow.modules.commercial.rfq.enums.RfqDirection;
import com.freightflow.modules.commercial.rfq.enums.RfqServiceType;
import com.freightflow.modules.commercial.rfq.enums.RfqStatus;
import com.freightflow.modules.commercial.rfq.enums.RfqTransportMode;
import com.freightflow.modules.customer.Customer;
import com.freightflow.modules.customer.CustomerRepository;
import com.freightflow.modules.port.Port;
import com.freightflow.modules.port.PortRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Commercial persistence integration")
@Testcontainers(disabledWithoutDocker = true)
class CommercialPersistenceIntegrationTest extends AbstractIntegrationTest {

    @Autowired private TenantRepository tenantRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PortRepository portRepository;
    @Autowired private RfqRepository rfqRepository;
    @Autowired private QuotationRepository quotationRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private PlatformTransactionManager transactionManager;

    @Test
    @DisplayName("deveAplicarFkCompostaPorTenantNaPersistenciaDeQuotation")
    void deveAplicarFkCompostaPorTenantNaPersistenciaDeQuotation() {
        executeCrossTenantConstraintScenario("X");
    }

    @Test
    @DisplayName("deveAplicarFkTenantAwareEntreUserECliente")
    void deveAplicarFkTenantAwareEntreUserECliente() {
        Tenant tenantA = tenantRepository.save(new Tenant("Tenant User A", "tenant-user-a", "ua@test.com", "FREE"));
        Tenant tenantB = tenantRepository.save(new Tenant("Tenant User B", "tenant-user-b", "ub@test.com", "FREE"));

        Customer customerA = customerRepository.saveAndFlush(new Customer(tenantA, "Atlas Cargo"));

        assertThatThrownBy(() -> executeInRequiresNew(() -> {
            User invalidClient = new User("Client B", "client-b@test.com", "hash", User.UserRole.CLIENT, tenantB);
            invalidClient.setCustomer(customerA);
            userRepository.saveAndFlush(invalidClient);
            return null;
        })).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("deveAplicarFkTenantAwareParaApprovedByESentBy")
    void deveAplicarFkTenantAwareParaApprovedByESentBy() {
        Tenant tenantA = tenantRepository.save(new Tenant("Tenant Audit A", "tenant-audit-a", "audit-a@test.com", "FREE"));
        Tenant tenantB = tenantRepository.save(new Tenant("Tenant Audit B", "tenant-audit-b", "audit-b@test.com", "FREE"));

        User userA = userRepository.save(new User("User A", "audit-user-a@test.com", "hash", User.UserRole.ADMIN, tenantA));
        User userB = userRepository.save(new User("User B", "audit-user-b@test.com", "hash", User.UserRole.ADMIN, tenantB));

        Port origin = portRepository.save(new Port("BRAD1", "Audit Santos", "BR", "America/Sao_Paulo", null, null));
        Port destination = portRepository.save(new Port("NLAD1", "Audit Rotterdam", "NL", "Europe/Amsterdam", null, null));

        RequestForQuotation rfq = new RequestForQuotation(
                tenantA,
                "RFQ-AUDIT-1",
                "Maria",
                RfqDirection.EXPORT,
                RfqTransportMode.OCEAN,
                RfqServiceType.LCL,
                origin,
                destination,
                userA
        );
        rfq.setContactEmail("audit@test.com");
        rfq.setProspectCompanyName("Prospect Audit");
        rfq.setStatus(RfqStatus.UNDER_ANALYSIS);
        rfq = rfqRepository.saveAndFlush(rfq);
        UUID rfqId = rfq.getId();

        Quotation quotation = executeInRequiresNew(() -> {
            Quotation value = new Quotation(findTenant(tenantA.getId()), findRfq(rfqId), "Q-AUDIT-1", "USD", findUser(userA.getId()));
            value.setStatus(com.freightflow.modules.commercial.quotation.enums.QuotationStatus.APPROVED);
            return quotationRepository.saveAndFlush(value);
        });

        UUID quotationId = quotation.getId();

        assertThatThrownBy(() -> executeInRequiresNew(() -> {
            Quotation invalidApproved = quotationRepository.findById(quotationId).orElseThrow();
            invalidApproved.setApprovedBy(findUser(userB.getId()));
            invalidApproved.setApprovedAt(java.time.Instant.now());
            quotationRepository.saveAndFlush(invalidApproved);
            return null;
        })).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> executeInRequiresNew(() -> {
            Quotation invalidSent = quotationRepository.findById(quotationId).orElseThrow();
            invalidSent.setSentBy(findUser(userB.getId()));
            invalidSent.setSentAt(java.time.Instant.now());
            quotationRepository.saveAndFlush(invalidSent);
            return null;
        })).isInstanceOf(DataIntegrityViolationException.class);

        UUID validQuotationId = executeInRequiresNew(() -> {
            Quotation valid = quotationRepository.findById(quotationId).orElseThrow();
            valid.setApprovedBy(findUser(userA.getId()));
            valid.setApprovedAt(java.time.Instant.parse("2026-07-29T12:00:00Z"));
            valid.setSentBy(findUser(userA.getId()));
            valid.setSentAt(java.time.Instant.parse("2026-07-29T12:30:00Z"));
            return quotationRepository.saveAndFlush(valid).getId();
        });

        Quotation persisted = quotationRepository.findById(validQuotationId).orElseThrow();
        assertThat(persisted.getApprovedBy().getId()).isEqualTo(userA.getId());
        assertThat(persisted.getSentBy().getId()).isEqualTo(userA.getId());
    }

    private void executeCrossTenantConstraintScenario(String suffix) {
        Tenant tenantA = tenantRepository.save(new Tenant("Tenant " + suffix + " A", "tenant-" + suffix.toLowerCase() + "-a", "a-" + suffix.toLowerCase() + "@test.com", "FREE"));
        Tenant tenantB = tenantRepository.save(new Tenant("Tenant " + suffix + " B", "tenant-" + suffix.toLowerCase() + "-b", "b-" + suffix.toLowerCase() + "@test.com", "FREE"));

        User userA = userRepository.save(new User("User A", "user-a-" + suffix.toLowerCase() + "@test.com", "hash", User.UserRole.ADMIN, tenantA));
        User userB = userRepository.save(new User("User B", "user-b-" + suffix.toLowerCase() + "@test.com", "hash", User.UserRole.ADMIN, tenantB));

        Port origin = portRepository.save(new Port("BRS" + suffix + "1", "Santos Test " + suffix, "BR", "America/Sao_Paulo", null, null));
        Port destination = portRepository.save(new Port("NLR" + suffix + "1", "Rotterdam Test " + suffix, "NL", "Europe/Amsterdam", null, null));

        RequestForQuotation rfq = new RequestForQuotation(
                tenantA,
                "RFQ-" + suffix + "-1",
                "Maria",
                RfqDirection.EXPORT,
                RfqTransportMode.OCEAN,
                RfqServiceType.LCL,
                origin,
                destination,
                userA
        );
        rfq.setContactEmail("maria-" + suffix.toLowerCase() + "@test.com");
        rfq.setProspectCompanyName("Prospect " + suffix);
        rfq.setStatus(RfqStatus.UNDER_ANALYSIS);
        rfq = rfqRepository.saveAndFlush(rfq);

        UUID tenantAId = tenantA.getId();
        UUID tenantBId = tenantB.getId();
        UUID userAId = userA.getId();
        UUID userBId = userB.getId();
        UUID rfqId = rfq.getId();

        assertThatThrownBy(() -> executeInRequiresNew(() -> {
            Quotation invalidQuotation = new Quotation(
                    findTenant(tenantBId),
                    findRfq(rfqId),
                    "Q-" + suffix + "-1",
                    "USD",
                    findUser(userBId)
            );
            quotationRepository.saveAndFlush(invalidQuotation);
            return null;
        }))
                .isInstanceOf(DataIntegrityViolationException.class);

        UUID persistedQuotationId = executeInRequiresNew(() -> {
            Quotation validQuotation = new Quotation(
                    findTenant(tenantAId),
                    findRfq(rfqId),
                    "Q-" + suffix + "-2",
                    "USD",
                    findUser(userAId)
            );
            return quotationRepository.saveAndFlush(validQuotation).getId();
        });

        Quotation persistedQuotation = quotationRepository.findById(persistedQuotationId).orElseThrow();

        assertThat(persistedQuotation.getId()).isNotNull();
        assertThat(persistedQuotation.getTenant().getId()).isEqualTo(tenantAId);
        assertThat(persistedQuotation.getRfq().getId()).isEqualTo(rfqId);
    }

    private <T> T executeInRequiresNew(TransactionCallback<T> callback) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template.execute(status -> callback.execute());
    }

    private Tenant findTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId).orElseThrow();
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow();
    }

    private RequestForQuotation findRfq(UUID rfqId) {
        return rfqRepository.findById(rfqId).orElseThrow();
    }

    @FunctionalInterface
    private interface TransactionCallback<T> {
        T execute();
    }
}
