package com.example.PharmacyManagement.strategy;

import org.springframework.stereotype.Component;

/**
 * Context Class for Strategy Pattern - Payment Processor
 * 
 * This class is responsible for:
 * 1. Selecting the appropriate payment strategy based on payment method
 * 2. Executing the payment using the selected strategy
 * 
 * This demonstrates the Strategy Pattern:
 * - Client (PaymentService) doesn't need to know which strategy is used
 * - Strategy selection is centralized in one place
 * - Easy to add new payment methods without changing existing code
 */
@Component
public class PaymentProcessor {
    
    private PaymentStrategy strategy;
    
    /**
     * Select the payment strategy based on payment method
     * @param paymentMethod - the payment method (CASH, CARD, POINTS, etc.)
     */
    public void setStrategy(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method cannot be null or empty");
        }
        
        // Strategy Selection Logic
        String method = paymentMethod.toUpperCase().trim();
        
        switch (method) {
            case "CASH":
                this.strategy = new CashPaymentStrategy();
                System.out.println("Selected Strategy: Cash Payment");
                break;
                
            case "CARD":
            case "CREDIT_CARD":
            case "DEBIT_CARD":
                this.strategy = new CardPaymentStrategy();
                System.out.println("Selected Strategy: Card Payment");
                break;
                
            case "POINTS":
                this.strategy = new PointsPaymentStrategy();
                System.out.println("Selected Strategy: Points Payment");
                break;
                
            default:
                // Default to cash payment for unknown methods
                this.strategy = new CashPaymentStrategy();
                System.out.println("WARNING: Unknown payment method '" + paymentMethod + "', defaulting to Cash Payment");
                break;
        }
    }
    
    /**
     * Process the payment using the selected strategy
     * @param amount - the amount to be paid
     * @param patientId - the patient making the payment
     * @return true if payment is successful, false otherwise
     */
    public boolean processPayment(double amount, String patientId) {
        if (strategy == null) {
            throw new IllegalStateException("Payment strategy not set. Call setStrategy() first.");
        }
        
        // Validate amount
        if (amount < 0) {
            System.err.println("ERROR: Invalid payment amount: " + amount);
            return false;
        }
        
        // Execute the payment using the selected strategy
        return strategy.pay(amount, patientId);
    }
    
    /**
     * Get the current payment method name
     * @return the payment method name
     */
    public String getMethodName() {
        if (strategy == null) {
            return "NONE";
        }
        return strategy.getMethodName();
    }
}

