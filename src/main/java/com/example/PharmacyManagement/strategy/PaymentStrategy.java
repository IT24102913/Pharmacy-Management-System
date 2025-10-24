package com.example.PharmacyManagement.strategy;

/**
 * Strategy Pattern - Payment Strategy Interface
 * 
 * This interface defines the contract that all payment methods must follow.
 * Each payment method (Cash, Card, Points) will implement this interface
 * with their own specific payment processing logic.
 * 
 * Benefits:
 * - Easy to add new payment methods without modifying existing code
 * - Each payment method has its own class (Single Responsibility Principle)
 * - Payment logic is separated and maintainable
 */
public interface PaymentStrategy {
    
    /**
     * Process the payment using this specific payment method
     * @param amount - the amount to be paid
     * @param patientId - the patient making the payment
     * @return true if payment is successful, false otherwise
     */
    boolean pay(double amount, String patientId);
    
    /**
     * Get the name of this payment method
     * @return the payment method name (e.g., "CASH", "CARD", "POINTS")
     */
    String getMethodName();
}

