package com.tenco.spring_blog.reply;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog.board.Board;
import com.tenco.spring_blog.user.User;
import lombok.Data;

public class ReplyRequest {

    // 댓글 작성 DTO
    @Data
    public static class SaveDto {
        private Long boardId; // 댓글을 달 게시글 id (화면에는 hidden input 전달)
        private String comment; // 댓글 내용

        public void validate() {
            if (boardId == null) {
                throw new Exception400("게시글 정보가 없습니다");
            }
            if (comment == null || comment.trim().isEmpty()) {
                throw new Exception400("댓글 내용을 입력해주세요");
            }
            if (comment.length() > 500) {
                throw new Exception400("댓글은 500자 이하로 입력해주세요");
            }
        }

        public Reply toEntity(Board board, User user) {
            return Reply.builder()
                    .comment(this.comment)
                    .board(board)
                    .user(user)
                    .build();
        }
    }
}
