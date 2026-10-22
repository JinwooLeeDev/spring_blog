package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.util.Define;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
public class UserApiController {

    private final MailService mailService;
    private final UserService userService;

    // POST /api/email/send
    // 요청 바디 :  {"email": "aaa@naver.com"}
    @PostMapping("/api/email/send")
    public ResponseEntity<?> sendCode(@RequestBody UserRequest.EmailCheckDto emailCheckDto, HttpSession session) {
        emailCheckDto.validate();
        userService.checkEmailAvailable(emailCheckDto.getEmail()); // 이미 가입된 이메일이 있으면 400 던져야 함
        // SMTP 를 사용해서 인증 번호 발송 (구글 메일 서버) -> 사용자 이메일 수신함 (인증번호)
        mailService.sendVerificationCode(emailCheckDto.getEmail(), session);
        return ResponseEntity.ok(Map.of("message", "인증번호가 발송되었습니다 "
                + Define.CODE_EXPIRE_MINUTES + "분 안에 입력하세요"));
    }

    // POST /api/email/verify
    // 요청 바디 {"email": "a@naver.com", "code" :  "123456"}
    @PostMapping("/api/email/verify")
    public ResponseEntity<?> verifyCode(@RequestBody UserRequest.EmailCheckDto emailCheckDto,
                                        HttpSession session) {
        emailCheckDto.validateCode();
        boolean verified = mailService.verifyCode(emailCheckDto.getEmail(), emailCheckDto.getCode(), session);
        if (!verified) {
            return ResponseEntity.badRequest().body(Map.of("message", "인증번호가 일치하지 않습니다"));
        }
        return ResponseEntity.ok().body(Map.of("message", "인증되었습니다"));
    }


    // ========= 이 컨트롤러에서 발생한 예외는 JSON 으로 별도 응답 처리 ===========
    @ExceptionHandler(Exception400.class)
    public ResponseEntity<Map<String, String>> handleEx400(Exception400 e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    // 메일 발송 실패 등 그 밖의 오류 (500)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleEx500(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "메일 발송 중 오류가 발생했습니다."));
    }

}






