package com.tenco.spring_blog.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Getter
@NoArgsConstructor // 필수 (JPA 엔티티 생성시)
@AllArgsConstructor
@Table(name = "user_tb")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 같은 사용자명을 두 번 가입할 수 없도록 유니크 제약
    @Column(unique = true)
    private String username;
    @Setter
    private String password;
    @Column(unique = true)
    private String email;

    @CreationTimestamp // now()
    private Timestamp createdAt;

    // 프로필 이미지 파일 이름 (선택 사항이므로 null 가능)
    private String profileImage;

    // id 와 createdAt은 자동으로 채워지므로 빌더에서 제외
    @Builder
    public User(String username, String password, String email, String profileImage) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.profileImage = profileImage;
    }

    // 회원 정보 수정 (변경 감지용)
    public void update(String password) {
        this.password = password;
    }

    // 프로필 이미지 변경(null 이면 삭제) - 트랜잭션 안에서 호출하면 더티 체킹 발생 됨
    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

}
