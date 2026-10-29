package com.tenco.spring_blog.refund;

import com.tenco.spring_blog._core.util.MyDateUtil;
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
}

