package com.tenco.spring_blog.reply;

import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.Data;

public class ReplyResponse {

    // 댓글 목록 응답 DTO
    @Data
    public static class ListDto {
        // pk, 댓글 내용, 작성자 명, 포맷된 시간, 내 댓글 여부
        private Long id;
        private String comment;
        private String username;
        private String createdAt; // 포맷된 작성일
        private boolean isOwner;  // 로그인한 사용자가 작성자인지 (삭제 버튼 표시용)

        public ListDto(Reply reply, Long sessionUserId) {
            this.id = reply.getId();
            this.comment = reply.getComment();
            this.username =  reply.getUser().getUsername();
            if (reply.getCreatedAt() != null) {
                this.createdAt = MyDateUtil.timestampFormat(reply.getCreatedAt());
            }
            this.isOwner = reply.isOwner(sessionUserId);
        }
    }

}
