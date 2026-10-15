package com.tenco.spring_blog.reply;

import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.Getter;

public class ReplyResponse {

    // 댓글 목록 응답 DTO
    @Getter
    public static class ListDto {
        private Long id;
        private String comment;
        private String username;
        private String createdAt;   // 포맷된 작성일
        private boolean isOwner;
        // 생성자

        public ListDto(Reply reply, Long sessionUserId) {
            this.id = reply.getId();
            this.comment = reply.getComment();
            this.username = reply.getUser().getUsername();
            if (reply.getCreatedAt() != null) {
                this.createdAt = MyDateUtil.timestampFormat(reply.getCreatedAt());
            }
            this.isOwner = reply.isOwner(sessionUserId);
        }
    }
}
