package com.tenco.spring_blog.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {


    @Query("SELECT p FROM Payment p WHERE p.paymentId = :paymentId")
    Optional<Payment> findByPaymentId(@Param("paymentId") String paymentId);

    @Query("""
        SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
        FROM Payment p 
        WHERE p.paymentId = :paymentId 
    """)
    boolean existsByPaymentId(@Param("paymentId") String paymentId);

    // 결제 내역 조회 (로그인 사용자)
    @Query("""
            SELECT p FROM Payment p
            WHERE p.user.id = :userId
            AND p.status IN :statusList
            ORDER BY p.id DESC
            """)
    List<Payment> findAllByUserIdAndStatusList(@Param("userId") Long userId, @Param("statusList") List<PaymentStatus> statusList);

    @Query("SELECT p.status FROM Payment p WHERE p.id = :id")
    Optional<PaymentStatus> findStatusById(@Param("id") Long id);

}
