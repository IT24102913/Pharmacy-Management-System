package com.example.PharmacyManagement.controller;

import com.example.PharmacyManagement.model.Patient;
import com.example.PharmacyManagement.model.Payment;
import com.example.PharmacyManagement.model.PointsTransaction;
import com.example.PharmacyManagement.repository.PatientRepository;
import com.example.PharmacyManagement.repository.PaymentRepository;
import com.example.PharmacyManagement.repository.PointsTransactionRepository;
import com.example.PharmacyManagement.repository.PrescriptionRepository;
import com.example.PharmacyManagement.service.LoyaltyService;
import com.example.PharmacyManagement.service.PatientService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LOYALTY MANAGEMENT CONTROLLER
 * 
 * Handles all loyalty program admin operations:
 * - View loyalty members and guests
 * - Manage memberships (upgrade/downgrade)
 * - View points balances and history
 * - Delete loyalty members and guests
 * - View loyalty program configuration
 * 
 * Team: Loyalty Management Team
 * Access: Admin only
 */
@Controller
@RequestMapping("/admin/loyalty")
public class LoyaltyController {

    private final LoyaltyService loyaltyService;
    private final PatientService patientService;
    private final PatientRepository patientRepository;
    private final PointsTransactionRepository pointsTransactionRepository;
    private final PaymentRepository paymentRepository;
    private final PrescriptionRepository prescriptionRepository;

    @Autowired
    public LoyaltyController(LoyaltyService loyaltyService, 
                           PatientService patientService,
                           PatientRepository patientRepository,
                           PointsTransactionRepository pointsTransactionRepository,
                           PaymentRepository paymentRepository,
                           PrescriptionRepository prescriptionRepository) {
        this.loyaltyService = loyaltyService;
        this.patientService = patientService;
        this.patientRepository = patientRepository;
        this.pointsTransactionRepository = pointsTransactionRepository;
        this.paymentRepository = paymentRepository;
        this.prescriptionRepository = prescriptionRepository;
    }

    /**
     * View all loyalty members with their points
     * Route: GET /admin/loyalty/members
     */
    @GetMapping("/members")
    public String viewLoyaltyMembers(@RequestParam(value = "userId", required = false) String userId, 
                                     Model model,
                                     HttpSession session) {
        // Admin authentication check
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        // Get all premium members
        List<Patient> members = loyaltyService.getAllPremiumMembers();
        model.addAttribute("members", members);
        model.addAttribute("totalMembers", members.size());

        // If specific member selected, show their details
        if (userId != null && !userId.isBlank()) {
            patientService.findByUserId(userId).ifPresent(patient -> {
                model.addAttribute("selectedMember", patient);
                
                // Get points history
                List<PointsTransaction> transactions = loyaltyService.getPointsHistory(userId);
                model.addAttribute("transactions", transactions);
                
                // Calculate stats
                int totalEarned = transactions.stream()
                    .filter(PointsTransaction::isEarned)
                    .mapToInt(PointsTransaction::getPoints)
                    .sum();
                int totalRedeemed = transactions.stream()
                    .filter(PointsTransaction::isRedeemed)
                    .mapToInt(PointsTransaction::getPoints)
                    .sum();
                
                model.addAttribute("totalEarned", totalEarned);
                model.addAttribute("totalRedeemed", totalRedeemed);
            });
        }

        return "loyalty-members";
    }

    /**
     * Downgrade member to regular customer
     * Route: POST /admin/loyalty/members/{userId}/downgrade
     */
    @PostMapping("/members/{userId}/downgrade")
    public String downgradeMember(@PathVariable String userId, RedirectAttributes redirectAttributes) {
        try {
            loyaltyService.removePremiumMembership(userId);
            redirectAttributes.addFlashAttribute("success", "Member downgraded successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to downgrade member: " + e.getMessage());
        }
        return "redirect:/admin/loyalty/members";
    }

    /**
     * Get member points balance (AJAX)
     * Route: GET /admin/loyalty/members/{userId}/points
     */
    @GetMapping("/members/{userId}/points")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMemberPoints(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int balance = loyaltyService.getPointsBalance(userId);
            double value = loyaltyService.getPointsDisplayValue(userId);
            
            response.put("success", true);
            response.put("balance", balance);
            response.put("displayValue", value);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get member points history (AJAX)
     * Route: GET /admin/loyalty/members/{userId}/history
     */
    @GetMapping("/members/{userId}/history")
    @ResponseBody
    public ResponseEntity<?> getMemberHistory(@PathVariable String userId) {
        try {
            List<PointsTransaction> history = loyaltyService.getPointsHistory(userId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }


    /**
     * View loyalty program configuration
     * Route: GET /admin/loyalty/config
     */
    @GetMapping("/config")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLoyaltyConfig() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("membershipFee", loyaltyService.getMembershipFee());
        config.put("minimumPointsBalance", loyaltyService.getMinimumPointsBalance());
        config.put("pointsToRupeeRate", loyaltyService.getPointsToRupeeRate());
        config.put("minimumBillForPoints", loyaltyService.getMinimumBillForPoints());
        config.put("pointsEarningRate", loyaltyService.getPointsEarningRate());
        
        return ResponseEntity.ok(config);
    }

    // ========== GUEST CUSTOMERS MANAGEMENT ==========

    /**
     * View all guest customers (non-premium members)
     * Route: GET /admin/loyalty/guests
     */
    @GetMapping("/guests")
    public String viewGuestCustomers(@RequestParam(value = "userId", required = false) String userId, 
                                     Model model,
                                     HttpSession session) {
        // Admin authentication check
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        List<Patient> guests = patientRepository.findByIsPremiumMember(false);
        model.addAttribute("guests", guests);

        if (userId != null && !userId.isBlank()) {
            patientRepository.findByUserId(userId).ifPresent(p -> {
                model.addAttribute("selectedGuest", p);
                List<Payment> payments = paymentRepository.findByPatientUserId(userId);
                payments.sort(Comparator.comparing(
                        (Payment pay) -> {
                            LocalDateTime dt = pay.getPaymentDate();
                            return dt != null ? dt : LocalDateTime.MIN;
                        },
                        Comparator.reverseOrder()
                ));
                model.addAttribute("payments", payments);
            });
        }

        return "guest-customers";
    }

    // ========== DELETION OPERATIONS ==========

    /**
     * Delete loyalty member completely (remove from database)
     * Route: POST /admin/loyalty/members/{userId}/delete
     */
    @PostMapping("/members/{userId}/delete")
    public String deleteLoyaltyMember(@PathVariable("userId") String userId, 
                                      RedirectAttributes redirectAttributes,
                                      HttpSession session) {
        // Admin authentication check
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        return deletePatient(userId, redirectAttributes, "redirect:/admin/loyalty/members", "loyalty member");
    }

    /**
     * Delete guest customer completely (remove from database)
     * Route: POST /admin/loyalty/guests/{userId}/delete
     */
    @PostMapping("/guests/{userId}/delete")
    public String deleteGuest(@PathVariable("userId") String userId, 
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {
        // Admin authentication check
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        return deletePatient(userId, redirectAttributes, "redirect:/admin/loyalty/guests", "guest");
    }

    /**
     * CASCADE DELETE: Remove patient and all related records
     * This is shared by both loyalty member and guest deletion
     * 
     * @param userId Patient user ID
     * @param redirectAttributes Flash messages
     * @param redirectUrl Where to redirect after deletion
     * @param patientType "loyalty member" or "guest" for messages
     * @return Redirect URL
     */
    private String deletePatient(String userId, RedirectAttributes redirectAttributes, 
                                String redirectUrl, String patientType) {
        try {
            Patient patient = patientRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

            // CASCADE DELETE: Remove all related records first
            try { 
                pointsTransactionRepository.deleteByPatientUserId(patient.getUserId()); 
            } catch (Exception ignored) {}
            
            try { 
                paymentRepository.deleteByPatientUserId(patient.getUserId()); 
            } catch (Exception ignored) {}
            
            try { 
                prescriptionRepository.deleteByPatientUserId(patient.getUserId()); 
            } catch (Exception ignored) {}

            // Finally delete the patient
            patientRepository.deleteById(patient.getUserId());
            
            redirectAttributes.addFlashAttribute("success", 
                "Deleted " + patientType + " " + patient.getName() + " (" + patient.getUserId() + ")");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to delete " + patientType + ": " + ex.getMessage());
        }
        
        return redirectUrl;
    }

    // ========== HELPER METHODS ==========

    /**
     * Check if logged-in user is admin
     */
    private boolean isAdmin(HttpSession session) {
        Object userRole = session.getAttribute("userRole");
        return userRole != null && "admin".equalsIgnoreCase((String) userRole);
    }
}

