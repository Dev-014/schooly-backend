package com.school.erp.service.accounting;

import com.school.erp.dto.accounting.AccountingSummaryResponse;
import com.school.erp.enums.accounting.AccountingPaymentMode;

import java.math.BigDecimal;
import java.util.List;

final class AccountingSummaryMapper {
    private AccountingSummaryMapper() {
    }

    static AccountingSummaryResponse fromGroupedAmounts(List<Object[]> rows) {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal cash = BigDecimal.ZERO;
        BigDecimal bank = BigDecimal.ZERO;
        BigDecimal online = BigDecimal.ZERO;

        for (Object[] row : rows) {
            String paymentMode = (String) row[0];
            String accountType = (String) row[1];
            BigDecimal amount = row[2] == null ? BigDecimal.ZERO : (BigDecimal) row[2];
            total = total.add(amount);

            switch (AccountingPaymentMode.categorize(paymentMode, accountType)) {
                case CASH -> cash = cash.add(amount);
                case BANK -> bank = bank.add(amount);
                case ONLINE -> online = online.add(amount);
            }
        }

        return new AccountingSummaryResponse(total, cash, bank, online);
    }
}
