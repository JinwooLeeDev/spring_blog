package com.tenco.spring_blog.board;


import com.tenco.spring_blog.user.User;
import com.tenco.spring_blog.util.MyDateUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Data
// 엔티티 클래스 만들기 : 데이터베이스 테이블 한 개를 자바 클래스로 그린 설계도
@Table(name = "board_tb")
@Entity
public class Board {

    @Id // 이 필드가 기본기 임을 나타냄
    // 기본키 값을 자동으로 생성 (IDENTITY 전략 -> DB 기본 설정 따른다) AUTO_INCREMENT 기능 사용
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 별도 어노테이션이 없으면 필드명이 컬럼명이 됨.
    private String title;
    private String content;

    //private String username;
    // N : 1 , 1 : N , N : M
    // LAZY 전략 , EAGER 전략
    // LAZY 전략 : 게시글을 조회할 때 사용자는 바로 조회하지 않고, 실제로 사용할 때 그 때 한번 더 조회
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // board_tb 에 만들어질 외래키 컬럼 이름 설정
    private User user;

    // now() <-- 사용하지 않아도 자동으로 PC --> DB 날짜 주입
    @CreationTimestamp
    private Timestamp createdAt; // created_at 컬럼(스프링이 기본값이 스네이크 케이스로 자동 변환 해 줌)

    // 비즈니스 로직을 위한 생성자 설계
    // id 와 createdAt은 JPA 자동으로 설정하므로 매개변수에서 제외
    @Builder
    public Board(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.user = user;
    }

    // 자신에 상태값을 변경하는 메서드 추가 (영속성 엔티티를 수정하는 메서드)
    public void update(BoardRequest.UpdateDto updateDto) {
        // 비즈니스 규칙 검증
        updateDto.validate();
        // 영속 상태에 있는 엔티티의 필드값을 여기서 변경
        this.title = updateDto.getTitle();
        this.content = updateDto.getContent();
        // 변경 감지 (Dirty Checking)
        // 1. 영속성 컨텍스트가 엔티티 최초 상태를 스냅샷으로 따로 보관
        // 2. 필드 값 변경 시 현재 시점 상태와 스냅샷 비교
        // 3. 트랜잭션 커밋 시점에 변경된 필드만 UPDATE 쿼리를 자동 생성
        // 4. UPDATE board_tb SET title = ?, content = ? WHERE id = ?
    }

    // 게시글 수정/삭제 권한 체크용 편의 메서드
    public boolean isOwner(Long userId) {
        return this.user.getId().equals(userId);
    }

    // 시간을 포맷을 메서드를 추가
    public String getTime() {
        return MyDateUtil.timestampFormat(createdAt);
    }
}
