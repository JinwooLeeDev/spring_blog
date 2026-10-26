package com.tenco.spring_blog.purchase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PurchaseJpaRepository extends JpaRepository<Purchase, Long> {
    // 구매 내역 조회 (해당 게시글 pk 기준)
    @Query("SELECT p FROM Purchase p WHERE p.user.id = :userId AND p.board.id = :boardId")
    Optional<Purchase> findByUserIdAndBoardId(@Param("userId") Long userId, @Param("boardId") Long boardId);

    // 구매 여부 확인 (true , false 응답 필요시)
    @Query("""
    SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
    FROM Purchase p 
    WHERE p.user.id = :userId AND p.board.id = :boardId
    """)
    boolean existsByUserIdAndBoardId(@Param("userId") Long userId, @Param("boardId") Long boardId);

    // 게시글을 구매한 건수 (게시글 삭제 가증 여부 확인)
    @Query("SELECT COUNT(p) FROM Purchase p WHERE p.board.id = :boardId")
    long countByBoardId(@Param("boardId") Long boardId);

    // 게시글에 달린 구매 내역 일괄 삭제 (게시글 삭제 전에 사용)
    @Modifying // SELECT 가 아니라면 반드시 설정 (JPQL 쿼리 작성 했을 때)
    @Query("DELETE FROM Purchase p WHERE p.board.id = :boardId")
    void deleteByBoardId(@Param("boardId") Long boardId);

}
