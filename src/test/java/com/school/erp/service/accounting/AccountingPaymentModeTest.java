package com.school.erp.service.accounting;

import com.school.erp.enums.accounting.AccountingPaymentMode;
import com.school.erp.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountingPaymentModeTest {

    @Test
    void canonicalizesSupportedModesAndRejectsUnsupportedValues() {
        assertEquals("Cash", AccountingPaymentMode.canonicalize("cash"));
        assertEquals("Bank Transfer", AccountingPaymentMode.canonicalize("bank"));
        assertEquals("Card", AccountingPaymentMode.canonicalize("Credit Card"));
        assertThrows(BadRequestException.class, () -> AccountingPaymentMode.canonicalize("Crypto"));
    }

    @Test
    void breakdownClassifiesEveryTransactionExactlyOnceUsingPaymentModeFirst() {
        var summary = AccountingSummaryMapper.fromGroupedAmounts(List.of(
                new Object[] {"Cash", "Bank", new BigDecimal("50000.00")},
                new Object[] {"Bank Transfer", "Bank", new BigDecimal("80000.00")},
                new Object[] {"UPI", "Cash", new BigDecimal("20000.00")},
                new Object[] {null, "Cash", new BigDecimal("1000.00")}
        ));

        assertEquals(new BigDecimal("151000.00"), summary.total());
        assertEquals(new BigDecimal("51000.00"), summary.cash());
        assertEquals(new BigDecimal("80000.00"), summary.bank());
        assertEquals(new BigDecimal("20000.00"), summary.online());
        assertEquals(summary.total(), summary.cash().add(summary.bank()).add(summary.online()));
    }
}
