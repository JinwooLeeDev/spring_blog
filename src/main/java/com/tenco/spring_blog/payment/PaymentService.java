package com.tenco.spring_blog.payment;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog.user.User;
import com.tenco.spring_blog.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentJpaRepository paymentJpaRepository;
    private final UserJpaRepository userJpaRepository;

    /**
     *  1단계 : 결제 건 생성
     *  paymentId 를 발급하고 "누가 얼마를 " 결제할 예정인지 READY 상태로 저장한다.
     */
    @Transactional
    public String prepare(Long sessionUserId, Integer amount) {
        User user = userJpaRepository.findById(sessionUserId)
                .orElseThrow(() -> new Exception400("사용자를 찾을 수 없습니다"));

        // 식별자가 겹치면 다시 만든다.
        String paymentId = generatePaymentId(sessionUserId);
        while (paymentJpaRepository.existsByPaymentId(paymentId)) {
            paymentId = generatePaymentId(sessionUserId);
        }
        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .user(user)
                .amount(amount)
                .build();
        paymentJpaRepository.save(payment);

        return paymentId;
    }

    private static String generatePaymentId(Long userId) {
        String paymentId = "point_" + userId +  "_"
                + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
        return paymentId;
    }

}







