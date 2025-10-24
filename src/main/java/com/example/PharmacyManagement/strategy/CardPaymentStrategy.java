package com.example.PharmacyManagement.strategy;

/**
 * Concrete Strategy - Card Payment
 * 
 * Handles credit/debit card payment processing.
 * In a real system, this might:
 * - Connect to payment gateway (Stripe, PayPal, etc.)
 * - Validate card details
 * - Process card transaction
 * - Handle authorization and settlement
 */
public class CardPaymentStrategy implements PaymentStrategy {
    
    @Override
    public boolean pay(double amount, String patientId) {
        System.out.println("==========================================");
        System.out.println("CARD PAYMENT PROCESSING");
        System.out.println("==========================================");
        System.out.println("Patient ID: " + patientId);
        System.out.println("Amount: Rs. " + String.format("%.2f", amount));
        System.out.println("Connecting to payment gateway...");
        System.out.println("Card authorized successfully");
        System.out.println("Transaction ID: TXN-" + System.currentTimeMillis());
        System.out.println("==========================================");
        
        // In real scenario: Call payment gateway API
        // Example: Stripe.charges.create(amount, currency, card)
        return true; // Payment successful
    }
    
    @Override
    public String getMethodName() {
        return "CARD";
    }
}

