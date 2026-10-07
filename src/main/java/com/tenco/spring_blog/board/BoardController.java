package com.tenco.spring_blog.board;

import com.tenco.spring_blog.user.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller // IoC
public class BoardController {

    private final BoardNativeRepository boardNativeRepository;
    private final BoardPersistRepository boardPersistRepository;

    // GET http://localhost:8080/  ,   http://localhost:8080/board/list
    @GetMapping({"/", "/board/list"})
    public String list(Model model) {
        List<Board> boardList = boardPersistRepository.findAll();
        model.addAttribute("boardList", boardList);
        return "board/list";
    }

    // GET - http://localhost:8080/board/3
    @GetMapping("/board/{id}")
    public String detail(@PathVariable(name = "id") Long id, Model model) {

        // Board boardEntity = boardPersistRepository.findById(id);
        Board boardEntity = boardPersistRepository.findByIdWithJQPL(id);
        if (boardEntity == null) {
            // 추후에 404 에러 페이지를 만들어서 처리할 예정
            throw new RuntimeException("게시글을 찾을 수 없습니다. :" + id);
        }
        model.addAttribute("board", boardEntity);
        return "board/detail";
    }

    // GET - http://localhost:8080/board/save
    @GetMapping("/board/save")
    public String saveForm(HttpSession session) {
        // 1. 인증 검사 : 로그인 안된 사용자는 이 페이지에 접근 못하게 처리
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        return "board/save-form";
    }

    @PostMapping("/board/save")
    // Spring 폼 데이터를 객체로 변환하는 과정 (데이터 바인딩 메커니즘)
    // 폼 데이터 바인딩 : Spring이 HTTP 요청 파라미터를 객체로 자동 변환
    public String save(BoardRequest.SaveDto saveDto, HttpSession session) {
        // 1. 인증 검사
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        // 2. 유효성 검사
        try {
            // 입력 데이터 검증
            saveDto.validate();
            // DTO 에서 Board 객체 생성
            Board board = saveDto.toEntity(sessionUser);
            // Board 저장
            Board savedBoard = boardPersistRepository.save(board);
            // 저장 성공 시 메인 페이지 이동
            return "redirect:/";
        } catch (Exception e) {
            // 검증 실패 시 메세지와 함께 작성 폼으로 돌아가기
            log.error(e.getMessage());
            return "board/save-form";
        }
        // 3.
    }

    // GET - http://localhost:8080/board/1/update
    @GetMapping("/board/{id}/update")
    public String updateForm(@PathVariable Long id, Model model) {

        // 수정하기 화면 요청 (먼저 조회부터)
        Board board = boardPersistRepository.findById(id);
        model.addAttribute("board", board);
        return "board/update-form";
    }

    // POST - http://localhost:8080/board/1/update (게시글 수정 기능 요청)
    @PostMapping("/board/{id}/update")
    public String update(@PathVariable Long id,
                         BoardRequest.UpdateDto reqDto) {

        reqDto.validate();  // 유효성 실패 (throw 던져짐)

        boardPersistRepository.updateById(id, reqDto);
        // PRG (Post Redirect Get)
        return "redirect:/board/" + id;     // 리다이렉트 수정된 게시글 상세보기 화면 이동
    }

    // 게시글 삭제
    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable Long id,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        // 1. 인증 검사 (로그인 여부 확인)
        // 2. 권한 확인 -- 로그인 했지만 내가 작성한 글 인지 여부 확인
        // 2.1 - 관리자 광고성 게시글 .. 삭제도 가능 (권한)
        User sessionUser = (User)session.getAttribute("sessionUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        try {
            // 2. 삭제할 게시글 조화 (권한 체크를 위해)
            Board boardEntity = boardPersistRepository.findById(id);
            // 3. 권한 체크 : 본인이 작성한 게시글만 삭제
            if (!boardEntity.isOwner(sessionUser.getId())) {
                redirectAttributes.addFlashAttribute(
                        "deleteError",
                        "삭제 권한이 없습니다. 본인이 작성한 게시글만 삭제할 수 있습니다."
                );
                return "redirect:/board/" + id;
            }
            // 4. 권한 확인 후 삭제 실행
            boardPersistRepository.deleteById(id);
            // 5. 삭제 성공 후 메인 페이지 리다이렉트
            return "redirect:/";
        } catch (Exception e) {
            log.error("삭제 실패 : {}", e.getMessage());
            //throw new RuntimeException(e);
            // 권한 없음 또는 기타 오류
            return "redirect:/board/" + id;
        }
    }
}
