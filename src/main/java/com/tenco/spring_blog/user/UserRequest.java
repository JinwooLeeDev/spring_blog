package com.tenco.spring_blog.user;

import lombok.Data;

@Data
public class UserRequest {

    // 회원가입용 DTO
    @Data
    public static class JoinDto {
        private String username;
        private String password;
        private String email;

        public void validate() {
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("사용자명은 필수 입니다.");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("비밀번호는 필수 입니다.");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("이메일은 필수 입니다.");
            }
            // 간단하게 이메일 형식 검증
            if (!email.contains("@")) {
                throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
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
                throw new IllegalArgumentException("사용자명은 필수 입니다.");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("비밀번호는 필수 입니다.");
            }
        }
    }

}
