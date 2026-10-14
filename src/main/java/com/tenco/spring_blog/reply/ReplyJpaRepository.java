package com.tenco.spring_blog.reply;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ReplyJpaRepository extends JpaRepository<Reply, Long> {
    // 기본적인 CRUD 기능 다 만들어 져 있음.
    /**
     *  1. 등록 및 수정 : save(Reply entity);
     *      - 엔티티를 DB에 저장합니다. ID가 없으면 INSERT, 있으면 UPDATE를 실행 함.
     *
     *  2. 단건 조회 :  findById(Long id)
     *      - ID로 엔티티를 조회하면 Optional<Reply> 타입을 반환합니다.
     *
     *  3. 전체 조회 : findAll();
     *      - 테이블의 모든 데이터를 조회하며 List<Reply>로 반환 됨
     *
     *  4. 삭제 : deleteById(Long id);
     *     - 특정 ID를 가진 엔티티를 삭제합니다.
     *
     *  5. 데이터 개수: count();
     *     - 전체 레코드의 개수를 반환합니다.
     *
     *  6. 존재 여부 확인 : existsById(Long id);
     *     - 해당 ID를 가진 데이터가 있는지 확인하여 boolean 을 반환합니다.
     *
     */

    // 게시글의 댓글 목록 조회 (작성자와 함께 조회, 오래된 순)
    @Query("SELECT r FROM Reply r JOIN FETCH r.user WHERE r.board.id = :boardId ORDER BY r.id ASC")
    List<Reply> findAllByBoardIdJoinUser(@Param("boardId") Long boardId);

     // 주의!!!  @Query 는 기본적으로 조회(SELECT)로 실행이 된다 DELETE, UPDATE 를 직접 쓸 때 반드시 @Modifying 사용
    // 게시글에 달린 댓글 일괄 삭제 (게시글 삭제 전에 사용 예정)
    @Modifying // 반드시 선언
    @Query("DELETE FROM Reply r WHERE r.board.id = :boardId")
    void deleteByBoardId(@Param("boardId") Long boardId);
}






