package com.tenco.spring_blog.refund;

import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.Data;
import lombok.Getter;

public class RefundResponse {
    @Getter
    public static class ListDto {
        private Long id;
        private int amount;
        private String paymentId;
        private String reason;
        private String requestedAt;
        private String statusDisplay;
        private String rejectReason;

        private boolean pending;
        private boolean approved;
        private boolean rejected;

        public ListDto(Refund refund) {
            this.id = refund.getId();
            this.amount = refund.getPayment().getAmount();
            this.paymentId = refund.getPayment().getPaymentId();
            this.reason = refund.getReason();
            if (refund.getCreatedAt() != null) {
                this.requestedAt = MyDateUtil.timestampFormat(refund.getCreatedAt());
            }
            this.statusDisplay = refund.getStatus().getDisplayName();
            this.pending = refund.getStatus() == RefundStatus.PENDING;
            this.approved = refund.getStatus() == RefundStatus.APPROVED;
            this.rejected = refund.getStatus() == RefundStatus.REJECTED;
            this.rejectReason = refund.getRejectReason();
        }
    }

    @Data
    public static class AdminListDto {
        private Long id;            // 환불 요청 PK
        private String username;    // 환불 신청자
        private String paymentId;   // 포트원 결제건 식별자
        private int amount;         // 환불 금액
        private String reason;      // 신청자 환불 사유
        private String statusDisplay;// 대기중, 승인됨, 거절됨
        private String requestedAt; // 환불 요청일

        private boolean pending;    // 모달 여부 (모달 버튼, 뱃지색)
        private boolean approved;   // 승인됨 여부 (뱃지 색)


        public AdminListDto(Refund refund) {
            this.id = refund.getId();
            this.username = refund.getUser().getUsername();
            this.paymentId = refund.getPayment().getPaymentId();
            this.amount = refund.getPayment().getAmount();
            this.reason = refund.getReason();
            this.statusDisplay = refund.getStatus().getDisplayName();
            if (refund.getCreatedAt() != null) {
                this.requestedAt = MyDateUtil.timestampFormat(refund.getCreatedAt());
            }
            this.pending = refund.getStatus() == RefundStatus.PENDING;
            this.approved = refund.getStatus() == RefundStatus.APPROVED;
        }
    }

    @Data
    public static class ApproveDto {
        private String paymentId; // 포트원 결제 건 식벌자 (경로로 활용 됨)
        private int amount; // DB 조회해야 알 수 있음

        public ApproveDto(String paymentId, int amount) {
            this.paymentId = paymentId;
            this.amount = amount;
        }
    }
}

