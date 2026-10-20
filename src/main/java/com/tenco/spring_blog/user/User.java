package com.tenco.spring_blog.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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

    // 사용자의 역할 목록(User 1 : UserRole N, 단방향)
    // [cascade, orphanRemoval]
    // 두 옵션은 부모 쪽의 @OneToMany 컬렉션에 붙인다. User 가 List<UserRole>
    // 을 가지고 있으므로 사용할 수 있다.
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false)
    private List<UserRole> roles = new ArrayList<>();


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

    // 역할 추가 (회원 가입 시 기본 USER 부여, 관리자는 직접 지정 )
    public void addRole(Role role) {
        // 이미 가진 역할이면 추가하지 않음 (코드에서 1차 방어)
        if (hasRole(role)) {
            return;
        }
        this.roles.add(UserRole.builder().role(role).build());
    }

    // 해당 역할을 가지고 있는지 확인
    public boolean hasRole(Role role) {
        for (UserRole u : this.roles) {
            if (u.getRole() == role) {
                return true; // 찾으면 바로 true
            }
        }
        return false; // 끝까지 없으면 false
    }

    // 로인한 사용자의 관리자 여부 확인 메서드
    public boolean isAdmin() {
        return hasRole(Role.ADMIN); // 고정 값 : 관리자 여부인지만 확인 true , false 반환
    }

}
