package com.tenco.spring_blog.board;

import com.tenco.spring_blog.user.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BoardJpaRepositoryTest {

    @Autowired
    private BoardJpaRepository boardJpaRepository;

    @Autowired
    private EntityManager entityManager;

    // 테스트용 사용자와 게시글을 직접 저장한다.

    private Board 게시글저장(String title, String content, String username) {
        User user = User.builder()
                .username(username) // 확인 필요: User에 username 필드와 빌더가 있다고 가정
                .email(username + "@test.com") // 확인 필요: User에 email 필드와 빌더가 있다고 가정
                .password("encoded-password") // 확인 필요: User에 password 필드와 빌더가 있다고 가정
                .build();

        entityManager.persist(user);
        entityManager.flush();

        Board board = Board.builder()
                .title(title) // 확인 필요: Board에 title 필드와 빌더가 있다고 가정
                .content(content) // 확인 필요: Board에 content 필드와 빌더가 있다고 가정
                .user(user) // 확인 필요: Board의 작성자 연관관계 필드명이 user라고 가정
                .build();

        entityManager.persist(board);
        entityManager.flush();

        return board;
    }

    @Test
    void findByIdJoinUser_게시글이존재하는_테스트() {
        // given: 조회할 게시글과 작성자를 저장한다.
        Board savedBoard = 게시글저장(
                "테스트 제목", "테스트 내용", "user1");

        // when: 게시글 ID로 작성자와 함께 조회한다.
        Optional<Board> result =
                boardJpaRepository.findByIdJoinUser(savedBoard.getId());

        // then: 조회된 게시글의 ID가 저장한 게시글 ID와 같은지 확인한다.
        assertThat(result)
                .isPresent()
                .get()
                .extracting(Board::getId)
                .isEqualTo(savedBoard.getId()); // 확인 필요: Board에 getId()가 있다고 가정
    }

    @Test
    void findByIdJoinUser_게시글이없는_테스트() {
        // given: 조회할 ID에 해당하는 게시글을 저장하지 않는다.
        Long nonexistentId = -1L;

        // when: 존재하지 않는 ID로 게시글을 조회한다.
        Optional<Board> result =
                boardJpaRepository.findByIdJoinUser(nonexistentId);

        // then: 조회 결과가 비어 있는지 확인한다.
        assertThat(result).isEmpty();
    }

    @Test
    void findByIdJoinUser_작성자함께로딩_테스트() {
        // given: 게시글을 저장하고 영속성 컨텍스트를 비운다.
        Board savedBoard = 게시글저장(
                "작성자 로딩 테스트", "테스트 내용", "user2");
        entityManager.flush();
        entityManager.clear();

        // when: JOIN FETCH 쿼리로 게시글을 다시 조회한다.
        Board foundBoard = boardJpaRepository
                .findByIdJoinUser(savedBoard.getId())
                .orElseThrow();

        // then: 작성자 user 연관관계가 로딩된 상태인지 확인한다.
        PersistenceUnitUtil util =
                entityManager.getEntityManagerFactory()
                        .getPersistenceUnitUtil();

        assertThat(util.isLoaded(foundBoard, "user")).isTrue();
    }

    @Test
    void findAllJoinUser_페이지크기_테스트() {
        // given: 서로 다른 게시글 5개를 저장하고 페이지 크기를 2로 설정한다.
        for (int i = 1; i <= 5; i++) {
            게시글저장("제목 " + i, "내용 " + i, "pageUser" + i);
        }

        PageRequest pageable = PageRequest.of(0, 2);

        // when: 첫 번째 페이지를 조회한다.
        Page<Board> result = boardJpaRepository.findAllJoinUser(pageable);

        // then: 첫 페이지에 게시글 2개가 반환되는지 확인한다.
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void findAllJoinUser_전체게시글개수_테스트() {
        // given: 게시글 5개를 저장하고 페이지 크기를 2로 설정한다.
        for (int i = 1; i <= 5; i++) {
            게시글저장("제목 " + i, "내용 " + i, "countUser" + i);
        }

        PageRequest pageable = PageRequest.of(0, 2);

        // when: 게시글 페이지를 조회한다.
        Page<Board> result = boardJpaRepository.findAllJoinUser(pageable);

        // then: 페이지에 포함된 게시글 수가 아니라 전체 게시글 수가 5인지 확인한다.
        assertThat(result.getTotalElements()).isEqualTo(5);
    }

    @Test
    void findAllJoinUser_전체페이지수_테스트() {
        // given: 게시글 5개를 저장하고 페이지 크기를 2로 설정한다.
        for (int i = 1; i <= 5; i++) {
            게시글저장(
                    "페이지 제목 " + i,
                    "페이지 내용 " + i,
                    "totalPageUser" + i
            );
        }

        PageRequest pageable = PageRequest.of(0, 2);

        // when: 게시글을 페이징 조회한다.
        Page<Board> result = boardJpaRepository.findAllJoinUser(pageable);

        // then: 전체 게시글 수가 5개인지 확인한다.
        assertThat(result.getTotalElements()).isEqualTo(8);

        // then: 전체 페이지 수가 3개인지 확인한다.
        assertThat(result.getTotalPages()).isEqualTo(4);
    }

    @Test
    void findAllJoinUser_정렬순서_테스트() {
        // given: 게시글 3개를 저장하고 ID 내림차순 정렬을 요청한다.
        for (int i = 1; i <= 3; i++) {
            게시글저장("정렬 제목 " + i, "정렬 내용 " + i, "sortUser" + i);
        }

        PageRequest pageable = PageRequest.of(
                0, 10, Sort.by(Sort.Direction.DESC, "id"));

        // when: ID 내림차순으로 게시글을 조회한다.
        Page<Board> result = boardJpaRepository.findAllJoinUser(pageable);

        // then: 반환된 게시글 ID가 내림차순인지 확인한다.
        List<Long> ids = result.getContent()
                .stream()
                .map(Board::getId)
                .toList();

        assertThat(ids)
                .isSortedAccordingTo((first, second) -> second.compareTo(first));
    }

    @Test
    void findAllKeywordJoinUser_제목일치_테스트() {
        // given: 제목에만 검색 키워드가 포함된 게시글을 저장한다.
        Board savedBoard = 게시글저장(
                "스프링 검색 테스트", "일반적인 게시글 내용", "titleUser");

        PageRequest pageable = PageRequest.of(0, 10);

        // when: 제목과 내용에서 키워드를 검색한다.
        Page<Board> result =
                boardJpaRepository.findAllKeywordJoinUser(pageable, "검색");

        // then: 제목이 일치하는 게시글이 조회되는지 확인한다.
        assertThat(result.getContent())
                .extracting(Board::getId)
                .contains(savedBoard.getId());
    }

    @Test
    void findAllKeywordJoinUser_내용일치_테스트() {
        // given: 내용에만 검색 키워드가 포함된 게시글을 저장한다.
        Board savedBoard = 게시글저장(
                "일반적인 제목", "JPA 검색 기능 테스트", "contentUser");

        PageRequest pageable = PageRequest.of(0, 10);

        // when: 내용에 포함된 키워드로 검색한다.
        Page<Board> result =
                boardJpaRepository.findAllKeywordJoinUser(pageable, "검색");

        // then: 내용이 일치하는 게시글이 조회되는지 확인한다.
        assertThat(result.getContent())
                .extracting(Board::getId)
                .contains(savedBoard.getId());
    }

    @Test
    void findAllKeywordJoinUser_일치하는게시글없음_테스트() {
        // given: 검색 키워드가 포함되지 않은 게시글을 저장한다.
        게시글저장("스프링 게시글", "JPA 관련 내용", "noMatchUser");

        PageRequest pageable = PageRequest.of(0, 10);

        // when: 제목과 내용에 없는 키워드로 검색한다.
        Page<Board> result =
                boardJpaRepository.findAllKeywordJoinUser(
                        pageable, "존재하지않는키워드");

        // then: 검색 결과가 비어 있는지 확인한다.
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findAllKeywordJoinUser_빈키워드_테스트() {
        // given: 검색할 게시글 3개를 저장하고 빈 키워드를 준비한다.
        for (int i = 1; i <= 3; i++) {
            게시글저장("빈 키워드 제목 " + i, "빈 키워드 내용 " + i, "emptyUser" + i);
        }

        PageRequest pageable = PageRequest.of(0, 10);

        // when: 빈 문자열로 게시글을 검색한다.
        Page<Board> result =
                boardJpaRepository.findAllKeywordJoinUser(pageable, "");

        // then: 모든 게시글이 검색되는지 확인한다.
        assertThat(result.getTotalElements()).isEqualTo(3);
    }
}