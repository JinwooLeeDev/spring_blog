package com.tenco.spring_blog.board;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

// RequiredArgsConstructor는 final 필드에 대한 생성자를 .class 파일 생성 시 자동으로 생성한다.
@RequiredArgsConstructor    // DI 처리
@Repository // IoC, Repository : 스프링이 데이터 접근 계층으로 인식, 데이터베이스 예외를 스프링 예외로 변환해준다.
public class BoardNativeRepository {

    // EntityManager JPA의 핵심 인터페이스
    // 데이터베이스와 모든 작업 담당
    private final EntityManager em;

    @Transactional
    public void save(String title, String content, String username) {
        Query query = em.createNativeQuery("insert into board_tb(title, content, username, created_at)" +
                "values(?, ?, ?, now())");

        query.setParameter(1, title);
        query.setParameter(2, content);
        query.setParameter(3, username);

        // SELECT, INSERT, UPDATE, DELETE
        query.executeUpdate();
    }

    public List<Board> findAll() {
        String sql = """
            select * from board_tb order by id desc
            """;
        Query query = em.createNativeQuery(sql, Board.class);
        // while(rs.next) ...
        return  query.getResultList();
    }

    public Board findById(Long id) {
        String sql = """
            select * from board_tb where id = ?
            """;
        Query query = em.createNativeQuery(sql, Board.class);
        query.setParameter(1, id);
        try {
            return (Board) query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public void deleteById(Long id) {
        String sql = """
                delete from board_tb where id = ?
                """;
        Query query = em.createNativeQuery(sql);
        query.setParameter(1, id);
        query.executeUpdate();
    }

    @Transactional
    public boolean updateById(String title, String content, Long id) {
        String sql = """
                update board_tb set title = ?, content = ? where id = ?
                """;
        Query query = em.createNativeQuery(sql);
        query.setParameter(1, title);
        query.setParameter(2, content);
        query.setParameter(3, id);
        int rows = query.executeUpdate();
        if (rows>0) {
            return true;
        } else {
            return false;
        }

    }

    // DI (생성자 주입)
    // public BoardNativeRepository(EntityManager em) {
    // this.em = em; }
}
