package com.example.PharmacyManagement.service;

import com.example.PharmacyManagement.model.Patient;
import com.example.PharmacyManagement.model.PointsTransaction;
import com.example.PharmacyManagement.repository.PatientRepository;
import com.example.PharmacyManagement.repository.PointsTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * LOYALTY MANAGEMENT SERVICE
 * 
 * Handles all loyalty program operations:
 * - Premium membership enrollment and management
 * - Points earning calculation
 * - Points redemption validation and processing
 * - Points transaction recording
 * - Loyalty member statistics
 * - Points transaction history
 * 
 * Team: Loyalty Management Team
 * 
 * Note: Points can only be earned through purchases or redeemed during checkout.
 *       No manual point adjustments allowed.
 */
@Service
public class LoyaltyService {

    private final PatientRepository patientRepository;
    private final PointsTransactionRepository pointsTransactionRepository;

    // ========== LOYALTY BUSINESS CONFIGURATION ==========
    
    /** Membership enrollment fee (one-time payment) */
    private final double membershipFee = 500.0;
    
    /** Minimum points balance that must be kept (cannot redeem below this) */
    private final int minimumPointsBalance = 50;
    
    /** Points to Rupee conversion rate for redemption */
    private final double pointsToRupeeRate = 1.0;  // 1 point = Rs. 1
    
    /** Minimum bill amount to earn points */
    private final double minimumBillForPoints = 500.0;  // Rs. 500
    
    /** Points earning rate (percentage of bill amount) */
    private final double pointsEarningRate = 0.05;  // 10% of bill

    @Autowired
    public LoyaltyService(PatientRepository patientRepository,
                          PointsTransactionRepository pointsTransactionRepository) {
        this.patientRepository = patientRepository;
        this.pointsTransactionRepository = pointsTransactionRepository;
    }

    // ========== CONFIGURATION GETTERS ==========
    
    public double getMembershipFee() {
        return membershipFee;
    }

    public int getMinimumPointsBalance() {
        return minimumPointsBalance;
    }

    public double getPointsToRupeeRate() {
        return pointsToRupeeRate;
    }

    public double getMinimumBillForPoints() {
        return minimumBillForPoints;
    }

    public double getPointsEarningRate() {
        return pointsEarningRate;
    }

    // ========== MEMBERSHIP MANAGEMENT ==========

    /**
     * Upgrade customer to premium membership
     * @param patient The customer to upgrade
     * @return true if upgraded, false if already premium
     */
    @Transactional
    public boolean upgradeToPremium(Patient patient) {
        if (patient.isPremiumMember()) {
            return false; // Already premium
        }
        
        patient.upgradeToPremium();
        patientRepository.save(patient);
        
        System.out.println("SUCCESS: Upgraded " + patient.getName() + " to premium membership");
        return true;
    }

    /**
     * Downgrade premium member to regular customer
     * @param userId Customer ID
     */
    @Transactional
    public void removePremiumMembership(String userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        patient.setPremiumMember(false);
        patient.setTotalPoints(0);
        patientRepository.save(patient);
        
        System.out.println("SUCCESS: Removed premium membership for " + patient.getName());
    }

    /**
     * Check if customer is eligible for premium benefits
     */
    public boolean isPremiumMember(String userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        return patient.isPremiumMember();
    }

    // ========== POINTS EARNING ==========

    /**
     * Calculate points earned for a bill amount
     * Only premium members earn points on bills > minimum threshold
     * 
     * @param patient The customer
     * @param billAmount Final bill amount after all discounts
     * @return Points earned (0 if not eligible)
     */
    public int calculatePointsEarned(Patient patient, double billAmount) {
        // Rule 1: Must be premium member
        if (!patient.isPremiumMember()) {
            return 0;
        }
        
        // Rule 2: Bill must exceed minimum threshold
        if (billAmount <= minimumBillForPoints) {
            return 0;
        }
        
        // Calculate: 10% of bill amount, rounded down
        int pointsEarned = (int) Math.floor(billAmount * pointsEarningRate);
        
        return pointsEarned;
    }

    /**
     * Award points to customer and record transaction
     * 
     * @param patient The customer
     * @param points Points to award
     * @param paymentId Reference payment ID
     */
    @Transactional
    public void awardPoints(Patient patient, int points, Long paymentId) {
        if (points <= 0) {
            return; // No points to award
        }
        
        if (!patient.isPremiumMember()) {
            System.out.println("WARNING: Cannot award points to non-premium member: " + patient.getName());
            return;
        }
        
        // Add points to customer
        patient.addPoints(points);
        patientRepository.save(patient);
        
        // Record transaction
        PointsTransaction transaction = new PointsTransaction();
        transaction.setPatientUserId(patient.getUserId());
        transaction.setPoints(points);
        transaction.setTransactionType("EARNED");
        transaction.setReferencePaymentId(paymentId);
        transaction.setDescription("Points earned from payment ID " + paymentId);
        pointsTransactionRepository.save(transaction);
        
        System.out.println("SUCCESS: Awarded " + points + " points to " + patient.getName() + 
                          " (New balance: " + patient.getTotalPoints() + ")");
    }

    // ========== POINTS REDEMPTION ==========

    /**
     * Validate if customer can redeem requested points
     * 
     * @param patient The customer
     * @param pointsRequested Points they want to use
     * @param billAmount Total bill amount
     * @return ValidationResult with adjusted points or error
     */
    public PointsRedemptionResult validatePointsRedemption(Patient patient, int pointsRequested, double billAmount) {
        PointsRedemptionResult result = new PointsRedemptionResult();
        
        // Validation 1: Must be premium member
        if (!patient.isPremiumMember()) {
            result.setValid(false);
            result.setErrorMessage("Only premium members can use points");
            return result;
        }
        
        // Validation 2: Must have minimum balance after redemption
        int availablePoints = patient.getTotalPoints();
        if (availablePoints <= minimumPointsBalance) {
            result.setValid(false);
            result.setErrorMessage("Insufficient points to redeem. Must keep a balance of " + 
                                  minimumPointsBalance + " points.");
            return result;
        }
        
        // Validation 3: Must have enough points
        if (availablePoints < pointsRequested) {
            result.setValid(false);
            result.setErrorMessage("Insufficient points. Available: " + availablePoints + 
                                  ", Requested: " + pointsRequested);
            return result;
        }
        
        // Validation 4: Calculate maximum redeemable
        int payableAmount = (int) Math.floor(billAmount);
        int redeemableOverReserve = Math.max(0, availablePoints - minimumPointsBalance);
        int maxRedeemable = Math.min(redeemableOverReserve, payableAmount);
        
        // Adjust points to maximum allowed
        int adjustedPoints = Math.min(pointsRequested, maxRedeemable);
        
        // Calculate discount amount
        double discount = adjustedPoints * pointsToRupeeRate;
        
        result.setValid(true);
        result.setAdjustedPoints(adjustedPoints);
        result.setDiscountAmount(discount);
        result.setRemainingBalance(availablePoints - adjustedPoints);
        
        return result;
    }

    /**
     * Redeem points and record transaction
     * 
     * @param patient The customer
     * @param points Points to redeem
     * @param paymentId Reference payment ID
     */
    @Transactional
    public void redeemPoints(Patient patient, int points, Long paymentId) {
        if (points <= 0) {
            return; // No points to redeem
        }
        
        // Redeem points (entity handles validation)
        boolean success = patient.redeemPoints(points);
        
        if (!success) {
            throw new RuntimeException("Failed to redeem points");
        }
        
        patientRepository.save(patient);
        
        // Record transaction
        PointsTransaction transaction = new PointsTransaction();
        transaction.setPatientUserId(patient.getUserId());
        transaction.setPoints(points);
        transaction.setTransactionType("REDEEMED");
        transaction.setReferencePaymentId(paymentId);
        transaction.setDescription("Redeemed points for payment ID " + paymentId);
        pointsTransactionRepository.save(transaction);
        
        System.out.println("SUCCESS: Redeemed " + points + " points from " + patient.getName() + 
                          " (New balance: " + patient.getTotalPoints() + ")");
    }

    // ========== POINTS QUERY ==========

    /**
     * Get customer's current points balance
     */
    public int getPointsBalance(String userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        return patient.getTotalPoints();
    }

    /**
     * Get customer's points transaction history
     */
    public List<PointsTransaction> getPointsHistory(String userId) {
        return pointsTransactionRepository.findByPatientUserIdOrderByTransactionDateDesc(userId);
    }

    /**
     * Get display value of points (for UI only, not redemption)
     */
    public double getPointsDisplayValue(String userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        return patient.getPointsValue(); // 100 points = Rs. 3.50
    }

    // ========== LOYALTY STATISTICS ==========

    /**
     * Get all premium members
     */
    public List<Patient> getAllPremiumMembers() {
        return patientRepository.findByIsPremiumMember(true);
    }

    // ========== HELPER CLASSES ==========

    /**
     * Result object for points redemption validation
     */
    public static class PointsRedemptionResult {
        private boolean valid;
        private int adjustedPoints;
        private double discountAmount;
        private int remainingBalance;
        private String errorMessage;

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public int getAdjustedPoints() {
            return adjustedPoints;
        }

        public void setAdjustedPoints(int adjustedPoints) {
            this.adjustedPoints = adjustedPoints;
        }

        public double getDiscountAmount() {
            return discountAmount;
        }

        public void setDiscountAmount(double discountAmount) {
            this.discountAmount = discountAmount;
        }

        public int getRemainingBalance() {
            return remainingBalance;
        }

        public void setRemainingBalance(int remainingBalance) {
            this.remainingBalance = remainingBalance;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}

