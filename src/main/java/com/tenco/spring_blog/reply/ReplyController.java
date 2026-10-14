package com.tenco.spring_blog.reply;

import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
public class ReplyController {

    private final ReplyService replyService;

    // 댓글 작성
    @PostMapping("/reply/save")
    public String save(ReplyRequest.SaveDto saveDto, HttpSession session) {
        // 2. 유효성검사
        saveDto.validate();
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        replyService.save(saveDto, sessionUserDto);
        // 4. 리다이렉트 처리 ( PRG 설계)
        return "redirect:/board/" + saveDto.getBoardId();
    }

    // 댓글 삭제
    // 주소 설계 - http://localhost:8080/reply/{id}/delete
    @PostMapping("/reply/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam(name = "boardId") Long boardId,  HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        replyService.deleteById(id, sessionUserDto);
        // PRG 패턴
        return "redirect:/board/" + boardId;
    }

}
