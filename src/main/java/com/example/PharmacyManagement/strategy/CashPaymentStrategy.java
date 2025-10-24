package com.example.PharmacyManagement.strategy;

/**
 * Concrete Strategy - Cash Payment
 * 
 * Handles cash payment processing.
 * In a real system, this might:
 * - Open the cash drawer
 * - Print receipt
 * - Record cash transaction in register
 */
public class CashPaymentStrategy implements PaymentStrategy {
    
    @Override
    public boolean pay(double amount, String patientId) {
        System.out.println("==========================================");
        System.out.println("CASH PAYMENT PROCESSING");
        System.out.println("==========================================");
        System.out.println("Patient ID: " + patientId);
        System.out.println("Amount: Rs. " + String.format("%.2f", amount));
        System.out.println("Status: Cash received and verified");
        System.out.println("==========================================");
        
        // In real scenario: Open cash drawer, verify cash, etc.
        return true; // Payment successful
    }
    
    @Override
    public String getMethodName() {
        return "CASH";
    }
}

