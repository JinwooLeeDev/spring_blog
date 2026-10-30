package com.tenco.spring_blog.refund;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog.payment.Payment;
import com.tenco.spring_blog.payment.PaymentJpaRepository;
import com.tenco.spring_blog.payment.PaymentResponse;
import com.tenco.spring_blog.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentJpaRepository paymentJpaRepository;

    /**
     * 환불 요청 폼에 보여줄 결제 정보
     */
    public PaymentResponse.ListDto findRequestForm(Long paymentPk, Long userId) {
        // 환불 요청 가능 여부 확인
        Payment payment = checkRefundable(paymentPk, userId);
        // 환불 가능한 Payment 엔티티만 조회되었기 때문에 무조건 true 처리한다.
        return new PaymentResponse.ListDto(payment, true);
    }

    private Payment checkRefundable(Long paymentPk, Long userId) {
        // 1. payment 테이블에 결제 내역 조회
        Payment payment = paymentJpaRepository.findById(paymentPk)
                .orElseThrow(() -> new Exception404("결제 내역을 찾을 수 없습니다."));

        // 2. 본인 결제인지 확인
        if (!payment.isOwner(userId)) {
            throw new Exception403("본인 결제만 환불 요청할 수 있습니다.");
        }

        // 3. 결제 완료 상태만 환불 가능 (READY, CANCELLED 는 불가)
        if (!payment.isPaid()) {
            throw new Exception400("결제가 완료된 내역만 환불 요청할 수 있습니다.");
        }

        // 4. 이미 환불 요청한 결제인지 확인 (대기중, 승인됨, 거절됨 모두 신청 불가)
        if (refundRepository.findByPaymentPk(paymentPk).isPresent()) {
            throw new Exception400("이미 환불 요청한 결제입니다.");
        }

        return payment;
    }

    /**
     * 환불 요청 저장
     */
    @Transactional
    public void save(Long userId, RefundRequest.SaveDto saveDto) {
        // 1. 폼 요청 화면처럼 검사를 한번 더 실행 (주소 직접 요청, 중복 제출 방지)
        Payment payment = checkRefundable(saveDto.getPaymentPk(), userId);

        // 2. 환불 요청 저장
        Refund refund = Refund.builder()
                .user(payment.getUser())
                .reason(saveDto.getReason().trim())
                .payment(payment)
                .build();

        refundRepository.save(refund);
    }

    @Transactional(readOnly = true)
    public List<RefundResponse.ListDto> findAllByUserId(Long userId) {
        List<Refund> refundList = refundRepository.findAllByUserIdJoinPayment(userId);

        return refundList.stream().map(RefundResponse.ListDto::new).toList();
    }

    @Transactional(readOnly = true)
    public List<RefundResponse.AdminListDto> findAllForAdmin() {
        List<Refund> refundList = refundRepository.findAllJoinUserAndPayment();
        return refundList.stream().map(RefundResponse.AdminListDto::new).toList();
    }

    /**
     * 관리자 : 환불 요청 거절 (포트원 통신 없음)
     *
     * @param refundId
     * @param rejectReason
     */
    @Transactional
    public void reject(Long refundId, String rejectReason) {

        // 1. 넘겨받은 refundId 기준으로 조회부터 해야한다. 왜 ? 엔티티 정보를 업데이트 하기 위해서 (더티체킹)
        Refund refund = refundRepository.findById(refundId).orElseThrow(() ->
                new Exception404("환불 요청을 찾을 수 없습니다."));

        // 2. 이미 승인, 거절된 요청은 굳이 다시 처리하지 않는다.
        if (!refund.isPending()) {
            throw new Exception400("대기중인 환불 요청만 거절할 수 있습니다.");
        }

        refund.reject(rejectReason);
    }

    /**
     * 관리자 승인 1단계 : 승인 가능한지 검사 ( DB 만 조회)
     * @param refundId
     * @return : 포트원 결제 취소에 필요한 값 리턴 ??
     */
    public RefundResponse.ApproveDto checkApprove(Long refundId) {
        // 환불 엔티티
        Refund refund = refundRepository.findByIdJoinUserAndPayment(refundId)
                .orElseThrow(() -> new Exception404("환불 요청을 찾을 수 없습니다"));
        // 환불 상태 확인 (대기, 승인, 거절 ) 중 대기 상태만 가능
        if (!refund.isPending()) {
            throw new Exception400("대기 중인 환불 요청만 승인할 수 있습니다");
        }
        // 결제 (PAID, READY)
        Payment payment = refund.getPayment();
        if (!payment.isPaid()) {
            throw new Exception400("결제 완료 상태만 환불 신청할 수 있습니다");
        }
        // User 충전한 포인트를 이미 사용했다면 돌려 받을 포인트가 없어서 환불 불가 .
        User user = refund.getUser();
        if (user.getPoint() < payment.getAmount()) {
            throw new Exception400("포인트 잔액이 부족하여 환불 할 수 없습니다");
        }
        return new RefundResponse.ApproveDto(payment.getPaymentId(), payment.getAmount());
    }

    /**
     * 관리자 승인 4단계 : 포트원 결제 취소가 끝난 뒤 DB 반영
     * @param refundId
     */
    @Transactional
    public void approve(Long refundId) {
        Refund refund = refundRepository.findByIdJoinUserAndPayment(refundId)
                .orElseThrow(() -> new Exception404("환불 요청을 찾을 수 없습니다."));

        // 1 ~ 4번 사이에 다른 요청이 같은 건을 처리했을 수 있어 한번 더 확인
        if (!refund.isPending()) {
            throw new Exception400("대기중인 환불 요청만 환불 승인 신청 가능");
        }

        Payment payment = refund.getPayment();
        refund.getUser().deductPoint(payment.getAmount());
        payment.markCancelled();    // 상태값 변경 CANCELLED
        refund.approve();
    }
}
