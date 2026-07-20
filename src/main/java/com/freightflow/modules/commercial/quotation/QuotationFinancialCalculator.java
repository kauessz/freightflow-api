package com.freightflow.modules.commercial.quotation;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class QuotationFinancialCalculator {

    public static final int MONEY_SCALE = 2;
    public static final int RATE_SCALE = 6;
    public static final int PERCENTAGE_SCALE = 4;

    public ItemTotals calculateItemTotals(BigDecimal costAmountInSellingCurrency,
                                          BigDecimal sellingAmount,
                                          BigDecimal quantity) {
        BigDecimal totalCost = money(costAmountInSellingCurrency.multiply(quantity));
        BigDecimal totalSelling = money(sellingAmount.multiply(quantity));
        BigDecimal profitAmount = money(totalSelling.subtract(totalCost));
        BigDecimal marginPercentage = percentage(profitAmount, totalSelling);
        BigDecimal markupPercentage = percentage(profitAmount, totalCost);

        return new ItemTotals(totalCost, totalSelling, profitAmount, marginPercentage, markupPercentage);
    }

    public QuotationTotals calculateQuotationTotals(List<QuotationItem> items) {
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalSelling = BigDecimal.ZERO;

        for (QuotationItem item : items) {
            if (!item.isIncluded() || item.isOptional()) {
                continue;
            }
            totalCost = totalCost.add(item.getTotalCost());
            totalSelling = totalSelling.add(item.getTotalSelling());
        }

        totalCost = money(totalCost);
        totalSelling = money(totalSelling);
        BigDecimal profitAmount = money(totalSelling.subtract(totalCost));
        BigDecimal marginPercentage = percentage(profitAmount, totalSelling);
        BigDecimal markupPercentage = percentage(profitAmount, totalCost);

        return new QuotationTotals(totalCost, totalSelling, profitAmount, marginPercentage, markupPercentage);
    }

    public BigDecimal convertCost(BigDecimal costAmount, BigDecimal exchangeRate) {
        return money(costAmount.multiply(exchangeRate).setScale(MONEY_SCALE, RoundingMode.HALF_UP));
    }

    public BigDecimal normalizeExchangeRate(BigDecimal exchangeRate) {
        return exchangeRate.setScale(RATE_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal percentage(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);
        }
        return numerator
                .divide(denominator, PERCENTAGE_SCALE + 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);
    }

    public record ItemTotals(
            BigDecimal totalCost,
            BigDecimal totalSelling,
            BigDecimal profitAmount,
            BigDecimal marginPercentage,
            BigDecimal markupPercentage
    ) {}

    public record QuotationTotals(
            BigDecimal costTotal,
            BigDecimal sellingTotal,
            BigDecimal profitAmount,
            BigDecimal marginPercentage,
            BigDecimal markupPercentage
    ) {}
}
