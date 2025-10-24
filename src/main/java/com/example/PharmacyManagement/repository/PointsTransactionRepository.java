package com.example.PharmacyManagement.repository;

import com.example.PharmacyManagement.model.PointsTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PointsTransactionRepository extends JpaRepository<PointsTransaction, Long> {
    List<PointsTransaction> findByPatientUserIdOrderByTransactionDateDesc(String patientUserId);

    @Transactional
    void deleteByPatientUserId(String patientUserId);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM PointsTransaction pt WHERE pt.referencePaymentId = :paymentId")
    void deleteByReferencePaymentId(@Param("paymentId") Long paymentId);
    
    @Query("SELECT pt FROM PointsTransaction pt WHERE pt.referencePaymentId = :paymentId")
    List<PointsTransaction> findByReferencePaymentId(@Param("paymentId") Long paymentId);
}


