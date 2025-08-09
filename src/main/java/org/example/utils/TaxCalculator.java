package org.example.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TaxCalculator {
    private static final BigDecimal TAX_RATE = new BigDecimal("0.07");

    public static BigDecimal calculateTax(BigDecimal subtotal) {
        return subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateTotalWithTax(BigDecimal subtotal) {
        return subtotal.add(calculateTax(subtotal));
    }
}
