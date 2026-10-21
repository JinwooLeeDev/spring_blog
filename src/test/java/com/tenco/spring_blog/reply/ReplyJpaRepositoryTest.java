package com.tenco.spring_blog.reply;

import com.tenco.spring_blog.board.Board; // 확인 필요: Board의 실제 패키지 경로
import com.tenco.spring_blog.user.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReplyJpaRepositoryTest {

    @Autowired
    private ReplyJpaRepository replyJpaRepository;

    @Autowired
    private EntityManager entityManager;

    private User 사용자저장(String username) {
        User user = User.builder()
                .username(username) // 확인 필요: User의 username 필드
                .email(username + "@test.com") // 확인 필요: User의 email 필드
                .password("encoded-password") // 확인 필요: User의 password 필드
                .build();

        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    private Board 게시글저장(String title, User user) {
        Board board = Board.builder()
                .title(title) // 확인 필요: Board의 title 필드
                .content("테스트 게시글 내용") // 확인 필요: Board의 content 필드
                .user(user) // 확인 필요: Board의 작성자 필드
                .build();

        entityManager.persist(board);
        entityManager.flush();
        return board;
    }

    private Reply 댓글저장(Board board, User user, String comment) {
        Reply reply = Reply.builder()
                .board(board) // 확인 필요: Reply의 board 연관관계
                .user(user) // 확인 필요: Reply의 user 연관관계
                .comment(comment) // 확인 필요: 댓글 내용 필드명
                .build();

        return replyJpaRepository.saveAndFlush(reply);
    }

    @Test
    void findAllByBoardIdJoinUser_댓글이존재하는_테스트() {
        // given: 사용자와 게시글을 저장하고 댓글을 등록한다.
        User user = 사용자저장("user1");
        Board board = 게시글저장("게시글 1", user);
        댓글저장(board, user, "첫 번째 댓글");

        // when: 게시글 ID로 댓글 목록을 조회한다.
        List<Reply> result =
                replyJpaRepository.findAllByBoardIdJoinUser(board.getId());

        // then: 댓글 한 개가 조회되는지 확인한다.
        assertThat(result).hasSize(1);
    }

    @Test
    void findAllByBoardIdJoinUser_다른게시글댓글제외_테스트() {
        // given: 두 게시글에 댓글을 각각 등록한다.
        User user = 사용자저장("user2");
        Board board1 = 게시글저장("게시글 1", user);
        Board board2 = 게시글저장("게시글 2", user);
        댓글저장(board1, user, "첫 번째 게시글 댓글");
        댓글저장(board2, user, "두 번째 게시글 댓글");

        // when: 첫 번째 게시글의 댓글을 조회한다.
        List<Reply> result =
                replyJpaRepository.findAllByBoardIdJoinUser(board1.getId());

        // then: 조회된 모든 댓글이 첫 번째 게시글에 속하는지 확인한다.
        assertThat(result)
                .hasSize(1)
                .allSatisfy(reply ->
                        assertThat(reply.getBoard().getId())
                                .isEqualTo(board1.getId()));
    }

    @Test
    void findAllByBoardIdJoinUser_댓글이없는_테스트() {
        // given: 댓글이 없는 게시글을 저장한다.
        User user = 사용자저장("user3");
        Board board = 게시글저장("댓글 없는 게시글", user);

        // when: 해당 게시글의 댓글을 조회한다.
        List<Reply> result =
                replyJpaRepository.findAllByBoardIdJoinUser(board.getId());

        // then: 빈 목록이 반환되는지 확인한다.
        assertThat(result).isEmpty();
    }

    @Test
    void findAllByBoardIdJoinUser_작성자함께로딩_테스트() {
        // given: 사용자와 게시글 및 댓글을 저장하고 영속성 컨텍스트를 비운다.
        User user = 사용자저장("user4");
        Board board = 게시글저장("게시글 4", user);
        댓글저장(board, user, "작성자 로딩 테스트");
        entityManager.flush();
        entityManager.clear();

        // when: JOIN FETCH 쿼리로 댓글을 조회한다.
        List<Reply> result =
                replyJpaRepository.findAllByBoardIdJoinUser(board.getId());

        // then: user 연관관계가 로딩되었는지 확인한다.
        PersistenceUnitUtil util =
                entityManager.getEntityManagerFactory()
                        .getPersistenceUnitUtil();

        assertThat(result).hasSize(1);
        assertThat(util.isLoaded(result.get(0), "user")).isTrue();
    }

    @Test
    void findAllByBoardIdJoinUser_오래된순서조회_테스트() {
        // given: 같은 게시글에 댓글을 순서대로 저장한다.
        User user = 사용자저장("user5");
        Board board = 게시글저장("게시글 5", user);

        Reply first = 댓글저장(board, user, "첫 번째 댓글");
        Reply second = 댓글저장(board, user, "두 번째 댓글");
        Reply third = 댓글저장(board, user, "세 번째 댓글");

        // when: 게시글의 댓글 목록을 조회한다.
        List<Reply> result =
                replyJpaRepository.findAllByBoardIdJoinUser(board.getId());

        // then: 댓글 ID가 오름차순으로 반환되는지 확인한다.
        assertThat(result)
                .extracting(Reply::getId)
                .containsExactly(first.getId(), second.getId(), third.getId());
    }

    @Test
    void deleteByBoardId_해당게시글댓글삭제_테스트() {
        // given: 두 게시글에 댓글을 각각 저장한다.
        User user = 사용자저장("user6");
        Board board1 = 게시글저장("게시글 6", user);
        Board board2 = 게시글저장("게시글 7", user);
        댓글저장(board1, user, "삭제 대상 댓글");
        댓글저장(board2, user, "유지 대상 댓글");

        // when: 첫 번째 게시글의 댓글을 삭제하고 영속성 컨텍스트를 비운다.
        replyJpaRepository.deleteByBoardId(board1.getId());
        entityManager.flush();
        entityManager.clear();

        List<Reply> deletedReplies =
                replyJpaRepository.findAllByBoardIdJoinUser(board1.getId());
        List<Reply> remainingReplies =
                replyJpaRepository.findAllByBoardIdJoinUser(board2.getId());

        // then: 대상 게시글의 댓글만 삭제되었는지 확인한다.
        assertThat(deletedReplies).isEmpty();
        assertThat(remainingReplies).hasSize(1);
    }

    @Test
    void deleteByBoardId_댓글이없는게시글_테스트() {
        // given: 댓글이 없는 게시글을 저장한다.
        User user = 사용자저장("user7");
        Board board = 게시글저장("댓글 없는 게시글", user);

        // when: 해당 게시글의 댓글 삭제를 실행한다.
        replyJpaRepository.deleteByBoardId(board.getId());

        // then: 댓글 목록이 빈 상태인지 확인한다.
        assertThat(
                replyJpaRepository.findAllByBoardIdJoinUser(board.getId())
        ).isEmpty();
    }
}