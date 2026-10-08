package com.tenco.spring_blog.board;

import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller // IoC
public class BoardController {

    private final BoardService boardService;

    @GetMapping({"/", "/board/list"})
    public String list(Model model) {
        List<Board> boardList = boardService.findAll();
        model.addAttribute("boardList", boardList);
        return "board/list";
    }

    // GET - http://localhost:8080/board/3
    // excludePathPatterns로 제외되어 로그인 없이 접근 가능
    @GetMapping("/board/{id}")
    public String detail(@PathVariable(name = "id") Long id, Model model) {
        Board board = boardService.findById(id);
        model.addAttribute("board", board);
        return "board/detail";
    }

    // GET - http://localhost:8080/board/save
    @GetMapping("/board/save")
    public String saveForm(HttpSession session) {
        return "board/save-form";
    }

    @PostMapping("/board/save")
    public String save(BoardRequest.SaveDto saveDto, HttpSession session) {
        saveDto.validate();
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        boardService.save(saveDto, sessionUser);
        return "redirect:/";

    }

    // TODO 1. 추후 인가처리를 서비스단으로 이동 예정
    // GET - http://localhost:8080/board/1/update
    @GetMapping("/board/{id}/update")
    public String updateForm(@PathVariable Long id, Model model, HttpSession session) {
        Board board = boardService.findById(id);
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);

        if (!board.isOwner(sessionUser.getId())) {
            throw new Exception403("수정할 권한이 없습니다.");
        }
        model.addAttribute("board", board);
        return "board/update-form";
    }

    // POST - http://localhost:8080/board/1/update (게시글 수정 기능 요청)
    @PostMapping("/board/{id}/update")
    public String update(@PathVariable Long id,
                         BoardRequest.UpdateDto updateDto,
                         HttpSession session) {
        updateDto.validate();
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        boardService.updateById(id, updateDto, sessionUser);
        return "redirect:/board/" + id;
    }

    // 게시글 삭제
    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable Long id,
                         HttpSession session) {
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        boardService.deleteById(id, sessionUser);
        return "redirect:/";
    }
}
