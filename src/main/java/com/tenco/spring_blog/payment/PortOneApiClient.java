package com.tenco.spring_blog.payment;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


// 포트원 서버와 HTTP 통신만 담당하는 클래스 (포트원 설정값도 이곳에서만 관리)
@Slf4j
@Getter
@Component
public class PortOneApiClient {

    @Value("${portone.store-id}")
    private String storeId;
    @Value("${portone.channel-key}")
    private String channelKey;
    @Value("${portone.api-secret}")
    private String apiSecret;

    // 여러 요청에서 함께 써도 안전하므로 하나만 만들어 재사용
    private static final String PAYMENT_URL = "https://api.portone.io/payments";
    private final RestTemplate restTemplate = new RestTemplate();

    // 포트원 서버에 결제 단건 조회
    // GET , 헤더 설정
    public PaymentResponse.PortOnePayment getPayment(String paymentId) {
        HttpHeaders headers = new HttpHeaders();
        // 반드시 공백 한칸 ! (PortOne )
        headers.set(HttpHeaders.AUTHORIZATION, "PortOne " + apiSecret);
        // GET 요청이라 바디 없이 헤더만 담는다
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<PaymentResponse.PortOnePayment> response = restTemplate.exchange(
                PAYMENT_URL + "/" + paymentId,
                HttpMethod.GET,
                request,
                PaymentResponse.PortOnePayment.class
        );
        log.info("포드원 결제 조회 완료 : {}", response);
        return response.getBody();
    }


    // 포트원 서버에 결제 취소 요청
    // amount 를 넣으면 그 금액만큼 취소 됨(부분 환불 가능 함), 넣지 않으면 전액 취소된다.
    // 이번 로직은 결제금액 전체를 넣는다.
    public PaymentResponse.PortOneCancel cancelPayment(String paymentId, int amount) {

        // 헤더 생성
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "PortOne " + apiSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 요청 바디 {"amount":5000,"reason":"관리자 환불 승인"}
        Map<String, Object> body = Map.of("amount", amount, "reason", "관리자 환불 승인");

        // HTTP 엔티티 생성 - 바디 + 헤더
        HttpEntity<Map<String, Object>> request = new HttpEntity(body, headers);

        // HTTP 요청 + 응답 처리
        ResponseEntity<PaymentResponse.PortOneCancel> response = restTemplate.exchange(
                PAYMENT_URL + "/" + paymentId + "/cancel",
                HttpMethod.POST,
                request,
                PaymentResponse.PortOneCancel.class
        );

        return response.getBody();
    }
}
