package com.school.erp.enums.accounting;

import com.school.erp.exception.BadRequestException;

import java.util.Locale;

public enum AccountingPaymentMode {
    CASH("Cash"),
    BANK_TRANSFER("Bank Transfer"),
    ONLINE("Online"),
    UPI("UPI"),
    CHEQUE("Cheque"),
    CARD("Card");

    public enum Category {
        CASH,
        BANK,
        ONLINE
    }

    private final String label;

    AccountingPaymentMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static String canonicalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return switch (normalize(value)) {
            case "CASH" -> CASH.label;
            case "BANK", "BANKTRANSFER" -> BANK_TRANSFER.label;
            case "ONLINE" -> ONLINE.label;
            case "UPI" -> UPI.label;
            case "CHEQUE", "CHECK" -> CHEQUE.label;
            case "CARD", "CREDITCARD" -> CARD.label;
            default -> throw new BadRequestException("Unsupported payment mode: " + value);
        };
    }

    public static Category categorize(String paymentMode, String accountType) {
        if (paymentMode != null && !paymentMode.isBlank()) {
            switch (normalize(paymentMode)) {
                case "CASH":
                    return Category.CASH;
                case "BANK", "BANKTRANSFER", "CHEQUE", "CHECK":
                    return Category.BANK;
                case "ONLINE", "UPI", "CARD", "CREDITCARD":
                    return Category.ONLINE;
                default:
                    break;
            }
        }
        return "CASH".equals(normalize(accountType)) ? Category.CASH : Category.BANK;
    }

    private static String normalize(String value) {
        return value.replaceAll("[^A-Za-z]", "").toUpperCase(Locale.ROOT);
    }
}
