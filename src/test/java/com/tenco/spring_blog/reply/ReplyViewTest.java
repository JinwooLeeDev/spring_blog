package com.tenco.spring_blog.reply;

import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.board.BoardJpaRepository;
import com.tenco.spring_blog.user.SessionUserDto;
import com.tenco.spring_blog.user.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class ReplyViewTest {
    @Autowired WebApplicationContext context;
    @Autowired ReplyJpaRepository replies;
    @Autowired UserJpaRepository users;
    @Autowired BoardJpaRepository boards;
    MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void rendersAllSeedCommentsOnTheirOwnBoards() throws Exception {
        assertThat(replies.count()).isEqualTo(11);
        for (long boardId = 1; boardId <= 8; boardId++) {
            var result = mvc.perform(get("/board/" + boardId)).andExpect(status().isOk());
            var comments = replies.findAllByBoardIdJoinUser(boardId);
            for (Reply reply : comments) {
                result.andExpect(content().string(containsString(reply.getComment())))
                        .andExpect(content().string(containsString(reply.getUser().getUsername())));
            }
            if (comments.isEmpty()) {
                result.andExpect(content().string(containsString("아직 댓글이 없습니다.")));
            }
        }
        mvc.perform(get("/board/2"))
                .andExpect(content().string(not(containsString("축하드립니다! 자주 방문할게요."))));
    }

    @Test
    void onlyBoardOwnerCanDeleteBoardAndCascadeItsComments() throws Exception {
        mvc.perform(post("/board/1/delete")).andExpect(status().isUnauthorized());
        var otherUser = new SessionUserDto(users.findById(2L).orElseThrow());
        mvc.perform(post("/board/1/delete").sessionAttr(Define.SESSION_USER, otherUser))
                .andExpect(status().isForbidden());
        assertThat(boards.existsById(1L)).isTrue();
        assertThat(replies.findAllByBoardIdJoinUser(1L)).hasSize(3);

        var owner = new SessionUserDto(users.findById(1L).orElseThrow());
        mvc.perform(post("/board/1/delete").sessionAttr(Define.SESSION_USER, owner))
                .andExpect(redirectedUrl("/"));
        boards.flush();
        assertThat(boards.existsById(1L)).isFalse();
        assertThat(replies.findAllByBoardIdJoinUser(1L)).isEmpty();
        assertThat(replies.count()).isEqualTo(8);
        assertThat(boards.count()).isEqualTo(7);
        assertThat(users.count()).isEqualTo(4);
    }

    @Test
    void deleteButtonIsVisibleOnlyForCommentOwner() throws Exception {
        mvc.perform(get("/board/1"))
                .andExpect(content().string(not(containsString("/reply/1/delete"))));
        var boardOwner = new SessionUserDto(users.findById(1L).orElseThrow());
        mvc.perform(get("/board/1").sessionAttr(Define.SESSION_USER, boardOwner))
                .andExpect(content().string(not(containsString("/reply/1/delete"))));
        var commentOwner = new SessionUserDto(users.findById(2L).orElseThrow());
        mvc.perform(get("/board/1").sessionAttr(Define.SESSION_USER, commentOwner))
                .andExpect(content().string(containsString("/reply/1/delete")))
                .andExpect(content().string(not(containsString("/reply/2/delete"))));
    }

    @Test
    void deleteEnforcesOwnershipAndReturnsToBoard() throws Exception {
        mvc.perform(post("/reply/1/delete")).andExpect(status().isUnauthorized());
        var otherUser = new SessionUserDto(users.findById(1L).orElseThrow());
        mvc.perform(post("/reply/1/delete").sessionAttr(Define.SESSION_USER, otherUser))
                .andExpect(status().isForbidden());
        assertThat(replies.existsById(1L)).isTrue();
        var owner = new SessionUserDto(users.findById(2L).orElseThrow());
        mvc.perform(post("/reply/1/delete").sessionAttr(Define.SESSION_USER, owner))
                .andExpect(redirectedUrl("/board/1"));
        assertThat(replies.existsById(1L)).isFalse();
        mvc.perform(get("/board/1"))
                .andExpect(content().string(not(containsString("축하드립니다! 자주 방문할게요."))))
                .andExpect(content().string(containsString("좋은 글 많이 올라오면 좋겠네요.")));
    }

    @Test
    void newlySavedCommentAppearsAlongsideExistingComments() throws Exception {
        var sessionUser = new SessionUserDto(users.findById(2L).orElseThrow());
        mvc.perform(post("/reply/save").sessionAttr(Define.SESSION_USER, sessionUser)
                        .param("boardId", "1").param("comment", "새 댓글 <script>테스트</script>"))
                .andExpect(redirectedUrl("/board/1"));
        mvc.perform(get("/board/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("새 댓글 &lt;script&gt;테스트&lt;/script&gt;")))
                .andExpect(content().string(containsString("축하드립니다! 자주 방문할게요.")));
        assertThat(replies.count()).isEqualTo(12);
    }
}
