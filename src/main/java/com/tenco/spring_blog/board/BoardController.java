package com.tenco.spring_blog.board;

import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.purchase.PurchaseService;
import com.tenco.spring_blog.reply.ReplyResponse;
import com.tenco.spring_blog.reply.ReplyService;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;


@Slf4j
@RequiredArgsConstructor // DI 처리
@Controller
public class BoardController {

    private final BoardService boardService;
    private final ReplyService replyService;
    private final PurchaseService purchaseService;

    // 유료 게시글 구매 요청 URL 만들기
    // /board/{{board.id}}/purchase
    @PostMapping("/board/{id}/purchase")
    public String purchase(@PathVariable(name = "id") Long id, HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        purchaseService.purchase(id, sessionUserDto.getId());
        // 구매 후 상세보기로 이동 (이제 본문이 나와야 함)
        return "redirect:/board/" + id;
    }

    @GetMapping({"/", "/board/list"})
    public String list(@RequestParam(name = "page", defaultValue = "1") int page,
                       @RequestParam(name = "size", defaultValue = "3") int size,
                       @RequestParam(name = "keyword", defaultValue = "") String keyword,
            Model model) {
        BoardResponse.PageDto pageDto = boardService.findAllPage(page, size, keyword);
        model.addAttribute("boardPage", pageDto);
        model.addAttribute("keyword", keyword);
        model.addAttribute("isSearch", !keyword.isBlank());
        // 페이지 링크는 직접 URL 인코딩이 필요하다
        model.addAttribute("keywordParam", URLEncoder.encode(keyword, StandardCharsets.UTF_8));

        return "board/list";
    }

    // GET http://localhost:8080/board/3
    // excludePathPatterns로 제외되어 로그인 접근 가능
    @GetMapping("/board/{id}")
    public String detail(@PathVariable(name = "id") Long id, Model model, HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        // 로그인하지 않았으면 null 설정
        Long sessionUserId = (sessionUserDto == null) ? null : sessionUserDto.getId();
        BoardResponse.DetailDto detailDto = boardService.findById(id, sessionUserId);

        // 로그인했고, 작성자 본인일 때만 수정, 삭제 버튼 표시
        boolean isOwner = sessionUserDto != null && detailDto.getUserId().equals(sessionUserDto.getId());

        // 댓글 목록 조회
        List<ReplyResponse.ListDto> replyList = replyService.findAllByBoardId(id, sessionUserId);

        model.addAttribute("board", detailDto);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("replyList", replyList);

        return "board/detail";
    }

    // GET http://localhost:8080/board/save (화면 요청)
    @GetMapping("/board/save")
    public String saveForm(HttpSession session) {
        return "board/save-form";
    }

    @PostMapping("/board/save")
    public String save(BoardRequest.SaveDto saveDto, HttpSession session) {
        saveDto.validate();
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        boardService.save(saveDto, sessionUserDto);
        return "redirect:/";
    }

    // GET http://localhost:8080/board/1/update (화면 요청)
    @GetMapping("/board/{id}/update")
    public String updateForm(@PathVariable Long id, Model model, HttpSession session) {
        SessionUserDto sessionUserDto  = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        BoardResponse.UpdateFormDto updateFormDto = boardService.findByIdAndUpdateForm(id, sessionUserDto.getId());
        model.addAttribute("board", updateFormDto);
        return "board/update-form";
    }

    // POST http://localhost:8080/board/1/update (게시글 수정 기능 요청)
    @PostMapping("/board/{id}/update")
    public String update(@PathVariable Long id,
                         BoardRequest.UpdateDto updateDto, HttpSession session) {

        updateDto.validate();
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        boardService.updateById(id, updateDto, sessionUserDto);
        return "redirect:/board/" + id;
    }

    // 게시글 삭제
    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) throws Exception403 {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        boardService.deleteById(id, sessionUserDto);
        return "redirect:/";
    }
}
