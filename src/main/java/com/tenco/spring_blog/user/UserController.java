package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.util.Define;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@Slf4j
@Controller // IoC (제어의 역전) 싱글톤 패턴으로 관리한다
public class UserController {

    private final UserService userService;

    // GET - http://localhost:8080/join
    @GetMapping("/join")
    public String joinForm() {
        // templates/ << 컨텐츠 루트 경로
        return "user/join-form";
    }

    // POST - http://localhost:8080/join
    @PostMapping("/join")
    public String join(UserRequest.JoinDto joinDto) {
        joinDto.validate();
        userService.join(joinDto);
        return "redirect:/login";
    }

    // GET - http://localhost:8080/login
    @GetMapping("/login")
    public String loginForm() {
        return "user/login-form";
    }

    // POST - http://localhost:8080/login
    // 로그인처리 (예외적으로 POST 요청)
    @PostMapping("/login")
    public String login(UserRequest.LoginDto loginDto, HttpSession session) {
        loginDto.validate();
        User user = userService.login(loginDto);
        user.setPassword(null);
        session.setAttribute(Define.SESSION_USER, user);
        return "redirect:/";
    }

    // GET - http://localhost:8080/user/update
    @GetMapping("/user/update")
    public String updateForm(Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        User user = userService.findById(sessionUser.getId());
        model.addAttribute("user", userService.findById(user.getId()));
        return "user/update-form";
    }

    // POST - http://localhost:8080/user/update
    @PostMapping("/user/update")
    public String update(Model model, HttpSession session, UserRequest.UpdateDto updateDto) {
        // 1. 인증 검사
        updateDto.validate();
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        User updateUser = userService.updateById(sessionUser.getId(), updateDto);
        updateUser.setPassword(null);
        session.setAttribute(Define.SESSION_USER, updateUser);
        return "redirect:/";
    }

    // GET - http://localhost:8080/logout
    @GetMapping("/logout")
    public String logoutForm(HttpSession session) {
        session.invalidate(); // 현재 세션을 무효화해서 로그인 정보 제거
        return "redirect:/";
    }
}
