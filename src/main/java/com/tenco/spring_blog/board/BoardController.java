package com.tenco.spring_blog.board;

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
    public String saveForm() {
        return "board/save-form";
    }

    @PostMapping("/board/save")
    // Spring 폼 데이터를 객체로 변환하는 과정 (데이터 바인딩 메커니즘)
    // 폼 데이터 바인딩 : Spring이 HTTP 요청 파라미터를 객체로 자동 변환
    public String save(BoardRequest.SaveDto reqDto) {
        // TODO - 수정예정
        // 1. DTO에서 Entity 클래스 타입으로 변환 해주어야함
//        Board board = Board.builder()
//                .title(reqDto.getTitle())
//                .content(reqDto.getContent())
//                .user(reqDto.getUsername())
//                .build();
//                // new Board(reqDto.getTitle(), reqDto.getContent(), reqDto.getUsername());
//        Board boardEntity = boardPersistRepository.save(board);

        return "redirect:/";
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
    public String delete(@PathVariable Long id) {
        boardPersistRepository.deleteById(id);

        // PRG (Post Redirect Get) 패턴
        return "redirect:/";
    }
}
