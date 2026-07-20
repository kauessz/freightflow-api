package com.freightflow.modules.commercial.quotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("QuotationFinancialCalculator")
class QuotationFinancialCalculatorTest {

    private final QuotationFinancialCalculator calculator = new QuotationFinancialCalculator();

    @Test
    @DisplayName("deveCalcularTotaisEArredondamento")
    void deveCalcularTotaisEArredondamento() {
        var totals = calculator.calculateItemTotals(
                new BigDecimal("100.155"),
                new BigDecimal("150.357"),
                new BigDecimal("2")
        );

        assertThat(totals.totalCost()).isEqualByComparingTo("200.31");
        assertThat(totals.totalSelling()).isEqualByComparingTo("300.71");
        assertThat(totals.profitAmount()).isEqualByComparingTo("100.40");
    }

    @Test
    @DisplayName("deveRetornarMargemEMarkupZeroQuandoDenominadorZero")
    void deveRetornarMargemEMarkupZeroQuandoDenominadorZero() {
        var marginZero = calculator.calculateItemTotals(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE);
        var markupZero = calculator.calculateItemTotals(BigDecimal.ZERO, new BigDecimal("10"), BigDecimal.ONE);

        assertThat(marginZero.marginPercentage()).isEqualByComparingTo("0.0000");
        assertThat(marginZero.markupPercentage()).isEqualByComparingTo("0.0000");
        assertThat(markupZero.markupPercentage()).isEqualByComparingTo("0.0000");
    }

    @Test
    @DisplayName("deveCalcularMargemNegativa")
    void deveCalcularMargemNegativa() {
        var totals = calculator.calculateItemTotals(
                new BigDecimal("200"),
                new BigDecimal("150"),
                BigDecimal.ONE
        );

        assertThat(totals.profitAmount()).isEqualByComparingTo("-50.00");
        assertThat(totals.marginPercentage()).isLessThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("deveIgnorarItensOpcionaisENaoIncluidosNoTotalDaCotacao")
    void deveIgnorarItensOpcionaisENaoIncluidosNoTotalDaCotacao() {
        Quotation quotation = TestQuotationFactory.quotation();
        QuotationItem included = TestQuotationFactory.item(quotation, new BigDecimal("10"), new BigDecimal("15"), true, false);
        QuotationItem optional = TestQuotationFactory.item(quotation, new BigDecimal("5"), new BigDecimal("8"), true, true);
        QuotationItem excluded = TestQuotationFactory.item(quotation, new BigDecimal("7"), new BigDecimal("9"), false, false);

        quotation.addItem(included);
        quotation.addItem(optional);
        quotation.addItem(excluded);

        var totals = calculator.calculateQuotationTotals(List.of(included, optional, excluded));

        assertThat(totals.costTotal()).isEqualByComparingTo("10.00");
        assertThat(totals.sellingTotal()).isEqualByComparingTo("15.00");
    }

    @Test
    @DisplayName("deveAplicarSemanticaOficialDoCambio")
    void deveAplicarSemanticaOficialDoCambio() {
        BigDecimal converted = calculator.convertCost(new BigDecimal("100"), new BigDecimal("5.25"));

        assertThat(converted).isEqualByComparingTo("525.00");
    }

    static class TestQuotationFactory {
        static Quotation quotation() {
            com.freightflow.modules.auth.Tenant tenant = new com.freightflow.modules.auth.Tenant("Tenant", "tenant", "ops@test.com", "FREE");
            com.freightflow.modules.auth.User user = new com.freightflow.modules.auth.User("Op", "op@test.com", "hash", com.freightflow.modules.auth.User.UserRole.OPERATOR, tenant);
            com.freightflow.modules.port.Port origin = new com.freightflow.modules.port.Port("BRSSZ", "Santos", "BR", "America/Sao_Paulo", null, null);
            com.freightflow.modules.port.Port destination = new com.freightflow.modules.port.Port("NLRTM", "Rotterdam", "NL", "Europe/Amsterdam", null, null);
            com.freightflow.modules.commercial.rfq.RequestForQuotation rfq = new com.freightflow.modules.commercial.rfq.RequestForQuotation(
                    tenant, "RFQ-1", "Maria", com.freightflow.modules.commercial.rfq.enums.RfqDirection.EXPORT,
                    com.freightflow.modules.commercial.rfq.enums.RfqTransportMode.OCEAN,
                    com.freightflow.modules.commercial.rfq.enums.RfqServiceType.LCL, origin, destination, user
            );
            return new Quotation(tenant, rfq, "Q-1", "USD", user);
        }

        static QuotationItem item(Quotation quotation, BigDecimal totalCost, BigDecimal totalSelling, boolean included, boolean optional) {
            QuotationItem item = new QuotationItem(
                    com.freightflow.modules.commercial.quotation.enums.ChargeCategory.OTHER,
                    "Charge",
                    com.freightflow.modules.commercial.quotation.enums.ChargeScope.GENERAL,
                    "USD",
                    totalCost,
                    "USD",
                    totalSelling,
                    BigDecimal.ONE,
                    0
            );
            item.setCostAmountInSellingCurrency(totalCost);
            item.setTotals(totalCost, totalSelling, totalSelling.subtract(totalCost), BigDecimal.ZERO, BigDecimal.ZERO);
            item.setIncluded(included);
            item.setOptional(optional);
            item.setQuotation(quotation);
            return item;
        }
    }
}
