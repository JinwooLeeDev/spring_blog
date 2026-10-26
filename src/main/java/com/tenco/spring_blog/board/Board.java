package com.tenco.spring_blog.board;


import com.tenco.spring_blog._core.util.MyDateUtil;
import com.tenco.spring_blog.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Data
// 엔티티 클래스 만들기 : 데이터베이스 테이블 한 개를 자바 클래스로 그린 설계도
@Table(name = "board_tb")
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Board {

    // 유료 게시글 가격
    public static final int PREMIUM_PRICE = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean premium;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // board_tb 에 만들어질 외래키 컬럼 이름 설정
    private User user;

    @CreationTimestamp
    private Timestamp createdAt; // created_at 컬럼(스프링이 기본값이 스네이크 케이스로 자동 변환 해 줌)

    @Builder
    public Board(String title, String content, User user, boolean premium) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.premium = premium;
    }

    public void update(BoardRequest.UpdateDto updateDto) {
        updateDto.validate();
        this.title = updateDto.getTitle();
        this.content = updateDto.getContent();
        this.premium = updateDto.isPremium();
    }

    public boolean isOwner(Long userId) {
        return this.user.getId().equals(userId);
    }

    public String getTime() {
        return MyDateUtil.timestampFormat(createdAt);
    }

}
