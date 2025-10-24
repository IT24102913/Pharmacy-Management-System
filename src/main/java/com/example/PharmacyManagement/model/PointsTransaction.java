package com.example.PharmacyManagement.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "points_transactions")
public class PointsTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_user_id", nullable = false, length = 20)
    private String patientUserId;

    @Column(nullable = false)
    private Integer points;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType; // 'EARNED', 'REDEEMED'

    @Column(name = "reference_payment_id")
    private Long referencePaymentId;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    private String description;

    public PointsTransaction() {
        this.transactionDate = LocalDateTime.now();
    }

    public PointsTransaction(String patientUserId, Integer points, String transactionType, String description) {
        this();
        this.patientUserId = patientUserId;
        this.points = points;
        this.transactionType = transactionType;
        this.description = description;
    }

    /* Getters and Setters */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPatientUserId() { return patientUserId; }
    public void setPatientUserId(String patientUserId) { this.patientUserId = patientUserId; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public Long getReferencePaymentId() { return referencePaymentId; }
    public void setReferencePaymentId(Long referencePaymentId) { this.referencePaymentId = referencePaymentId; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isEarned() {
        return "EARNED".equalsIgnoreCase(transactionType);
    }

    public boolean isRedeemed() {
        return "REDEEMED".equalsIgnoreCase(transactionType);
    }
}
