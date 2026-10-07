package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.Exception400;
import lombok.Data;

@Data
public class UserRequest {

    // 회원정보 수정용 DTO
    @Data
    public static class UpdateDto {
        private String password;

        public void validate() {
            if (password == null || password.trim().isEmpty()) {
                throw new Exception400("비밀번호는 필수입니다.");
            }
            if (password.length() < 4) {
                throw new Exception400("비밀번호는 4글자 이상이어야 합니다.");
            }
            // 필요하다면 여기에 길이 수 제한, 특수문자 포함여부 (정규식) 활용 가능
        }

    }

    // 회원가입용 DTO
    @Data
    public static class JoinDto {
        private String username;
        private String password;
        private String email;

        public void validate() {
            if (username == null || username.trim().isEmpty()) {
                throw new Exception400("사용자명은 필수 입니다.");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new Exception400("비밀번호는 필수 입니다.");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new Exception400("이메일은 필수 입니다.");
            }
            // 간단하게 이메일 형식 검증
            if (!email.contains("@")) {
                throw new Exception400("올바른 이메일 형식이 아닙니다.");
            }
        }

        // DTO 에서 User 엔티티로 변환하는 메서드
        // 계층 간 데이터 변환을 명확하게 분리하는 것이 좋다.
        public User toEntity() {
            return new User(this.username, this.password, this.email);
        }
    }

    @Data
    public static class LoginDto {
        private String username;
        private String password;

        public void validate() {
            if (username == null || username.trim().isEmpty()) {
                throw new Exception400("사용자명은 필수 입니다.");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new Exception400("비밀번호는 필수 입니다.");
            }
        }
    }

}
