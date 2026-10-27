package com.tenco.spring_blog.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class PortOnePaymentService {

    private final PortOneApiClient portOneApiClient;
    private final PaymentService paymentService;

    public PaymentResponse.PrepareDto prepare(Long sessionUserId, Integer amount) {
        String paymentId = paymentService.prepare(sessionUserId, amount);

        return PaymentResponse.PrepareDto.builder()
                .paymentId(paymentId)
                .amount(amount)
                .storeId(portOneApiClient.getStoreId())
                .channelKey(portOneApiClient.getChannelKey())
                .build();
    }

    // 3단계 : 포트원 조회(트랜잭션 밖) -> 검증(포트원 서버) -> 충전(트랜잭션 안)
    public PaymentResponse.CompleteDto complete(Long id, String paymentId) {

        // 1. 포트원 서버에 실제 결제 조회 (외부 통신)

        // 2. 검증(포트원 응답 데이터 , 결제 DB 정보 비교 검증) --> 포인트 충전

    }
}
