package com.tenco.spring_blog.user;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
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
        userService.checkEmailAvailable(emailCheckDto.getEmail());
        // 이미 가입된 이메일이 있으면 400 던져야 함

        // SMTP 를 사용해서 인증 번호 발송 (구글 메일 서버) -> 사용자 이메일 수신함 (인증번호)
        mailService.sendVerificationCode(emailCheckDto.getEmail(), session);

        return ResponseEntity.ok(Map.of("message", "인증 되었습니다"));
    }

    // POST /api/email/verify
    // 요청 바디 {"email": "a@naver.com", "code" :  "123456"}
    @PostMapping("/api/email/verify")
    public ResponseEntity<?> verifyCode(@RequestBody UserRequest.EmailCheckDto emailCheckDto,
                                        HttpSession session) {
        emailCheckDto.validateCode();
        boolean verified =  mailService.verifyCode(emailCheckDto.getEmail(), emailCheckDto.getCode(), session);
        if (!verified) {
            return ResponseEntity.badRequest().body(Map.of("message", "인증번호가 일치하지 않습니다"));
        }
        return ResponseEntity.ok().body(Map.of("message", "인증되었습니다"));
    }

}






