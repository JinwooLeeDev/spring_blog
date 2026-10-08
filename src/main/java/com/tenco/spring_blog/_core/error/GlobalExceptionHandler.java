package com.tenco.spring_blog._core.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// 모든 컨트롤러에서 발생하는 예외를 이 클래스에서 처리 (중앙 집중화)
@Slf4j
@ControllerAdvice   // IoC
public class GlobalExceptionHandler {

    // 특정 예외 타입이 발생했을 때 실행될 메서드로 지정
    @ExceptionHandler(Exception400.class)
    public String ex400(Exception400 e, HttpServletRequest request, Model model) {
        log.warn("==== 400 Bad Request 에러 발생 ====");
        log.warn("요청 URL : {}", request.getRequestURL());
        log.warn("에러 메세지 : {}", e.getMessage());
        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());

        model.addAttribute("msg", e.getMessage());
        return "err/400";
    }

    @ExceptionHandler(Exception401.class)
    public String ex401(Exception401 e, HttpServletRequest request, RedirectAttributes rttr) {
        log.warn("==== 400 Unauthorized 에러 발생 ====");
        log.warn("요청 URL : {}", request.getRequestURL());
        log.warn("인증 오류 : {}", e.getMessage());
        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());

        rttr.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/login";
    }

//    @ExceptionHandler(Exception403.class)
//    @ResponseBody
//    public String ex403(Exception403 e, HttpServletRequest request, Model model) {
//        log.warn("==== 403 Bad Request 에러 발생 ====");
//        log.warn("요청 URL : {}", request.getRequestURL());
//        log.warn("권한 오류 : {}", e.getMessage());
//        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());
//
//        model.addAttribute("msg", e.getMessage());
//        return "err/403";
//    }

    @ExceptionHandler(Exception403.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN) // 상태코드 403 지정 (없으면 200으로 나감)
    public String ex403(Exception403 e, HttpServletRequest request, Model model) {
        log.warn("==== 403 Bad Request 에러 발생 ====");
        log.warn("요청 URL : {}", request.getRequestURL());
        log.warn("권한 오류 : {}", e.getMessage());
        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());

        String msg = e.getMessage().replace("'", "\\'"); // 따옴표 깨짐 방지

        return """
                <script>
                    alert('%s');
                    history.back();
                """.formatted(msg);
    }

    @ExceptionHandler(Exception404.class)
    public String ex404(Exception404 e, HttpServletRequest request, Model model) {
        log.warn("==== 404 Not Found 에러 발생 ====");
        log.info("요청 URL : {}", request.getRequestURL());
        log.info("권한 오류 : {}", e.getMessage());
        log.info("예외 클래스 : {}", e.getClass().getSimpleName());

        model.addAttribute("msg", e.getMessage());
        return "err/404";
    }


    @ExceptionHandler(Exception500.class)
    public String ex500(Exception500 e, HttpServletRequest request, Model model) {
        log.warn("==== 500 Internal Server Error 에러 발생 ====");
        log.warn("요청 URL : {}", request.getRequestURL());
        log.warn("서버 오류 : {}", e.getMessage());
        log.warn("스택 트레이스 :", e);   // 전체 스택 트레이스 포함

        model.addAttribute("msg", "네트워크 일시적 장애");
        return "err/500";
    }

    // 기타 모든 RuntimeException 처리 (최후의 보루)
    @ExceptionHandler(Exception.class)
    public String ex500(RuntimeException e, HttpServletRequest request, Model model) {
        log.warn("==== 예상치 못한 런타임 에러 발생 ====");
        log.warn("요청 URL : {}", request.getRequestURL());
        log.warn("에러 타입 : {}", e.getClass().getSimpleName());
        log.warn("에러 메세지 : {}", e.getMessage());
        log.warn("스택 트레이스 :", e);   // 전체 스택 트레이스 포함

        model.addAttribute("msg", "시스템 오류 발생, 관리자에게 문의해주세요.");
        return "err/500";
    }
}
