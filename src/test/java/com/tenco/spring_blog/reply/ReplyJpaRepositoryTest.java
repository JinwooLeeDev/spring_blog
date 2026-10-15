package com.tenco.spring_blog.reply;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ReplyJpaRepositoryTest {

    @Autowired
    private ReplyJpaRepository replyJpaRepository;

    @Test
    void findAllByBoardIdJoinUser_test() {

        // given
        Long boardId = 1L;

        // when
        List<Reply> replies =
                replyJpaRepository.findAllByBoardIdJoinUser(boardId);

        // then
        assertThat(replies).isNotNull();

        for (Reply reply : replies) {
            System.out.println("댓글 ID : " + reply.getId());
            System.out.println("댓글 작성자 : " + reply.getUser().getUsername());
            System.out.println("---------------------");
        }
    }
}