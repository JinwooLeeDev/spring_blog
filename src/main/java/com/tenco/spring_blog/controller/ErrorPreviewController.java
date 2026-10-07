package com.tenco.spring_blog.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Profile("local")
@Controller
public class ErrorPreviewController {

    @GetMapping("/preview/errors/{code:400|401|403|404|500}")
    public String preview(@PathVariable("code") String code, Model model) {
        String message = switch (code) {
            case "400" -> "입력한 내용을 다시 확인해 주세요.";
            case "401" -> "로그인 후 이용해 주세요.";
            case "403" -> "이 페이지에 접근할 권한이 없습니다.";
            case "404" -> "요청하신 페이지를 찾을 수 없습니다.";
            default -> "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.";
        };
        model.addAttribute("msg", message);
        return "err/" + code;
    }
}
