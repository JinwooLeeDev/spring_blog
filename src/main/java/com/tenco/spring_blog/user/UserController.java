package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.UnAuthorizedException;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.purchase.PurchaseResponse;
import com.tenco.spring_blog.purchase.PurchaseService;
import com.tenco.spring_blog.stos.PostDto;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RequiredArgsConstructor // DI
@Slf4j
@Controller // IoC (제어의 역전) 싱글톤 패턴으로 관리 됨.
public class UserController {

    private final UserService userService;
    private final KakaoLoginService kakaoLoginService;
    private final PurchaseService purchaseService;

    // GET http://localhost:8080/user/payment/list (결제 내역 화면)
    @GetMapping("/user/payment/list")
    public String paymentList() {
        return "user/payment-list";
    }

    // GET http://localhost:8080/user/purchase/list (구매 내역 화면)
    @GetMapping("/user/purchase/list")
    public String purchaseList(Model model, HttpSession session) {

        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        List<PurchaseResponse.ListDto> purchaseList = purchaseService.findAllByUserId(sessionUserDto.getId());
        model.addAttribute("purchaseList", purchaseList);
        return "user/purchase-list";
    }


    // GET http://localhost:8080/user/point/charge (포인트 충전 화면)
    @GetMapping("/user/point/charge")
    public String chargePointForm(Model model, HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        model.addAttribute("user", sessionUserDto);
        return "user/charge-point";
    }



    @GetMapping("/kakao-login")
    public String kakaoLogin() {
        return "redirect:" + kakaoLoginService.getAuthorizeUrl();
    }

    // 리다이렉트로 들어와서 카카오 서버가 code 값을 동적으로 꼽아주는 URL 입니다
    @GetMapping("/oauth/kakao")
    public String kakaoCallback(@RequestParam(name = "code", required = false) String code,
                                HttpSession session ) {
         SessionUserDto sessionUserDto = kakaoLoginService.login(code);
         session.setAttribute(Define.SESSION_USER, sessionUserDto);
         return "redirect:/";
    }

    // 프로필 삭제
    @PostMapping("/user/profile-image/delete")
    public String deleteProfileImage(HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        userService.deleteProfileImage(sessionUserDto.getId());
        return "redirect:/user/detail";
    }

    // GET http://localhost:8080/user/detail
    @GetMapping("/user/detail")
    public String detail(Model model, HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        UserResponse.DetailDto user = userService.findDetail(sessionUserDto.getId());
        model.addAttribute("user", user);
        return "user/detail";
    }

    // GET http://localhost:8080/join
    @GetMapping("/join")
    public String joinForm() {
        return "user/join-form";
    }

    // POST http://localhost:8080/join
    @PostMapping("/join")
    public String join(UserRequest.JoinDto joinDto, Model model, HttpSession session) {
        joinDto.validate();
        // 이메일 인증 완료된 값 꺼내 오기 (세션에 저장된 상태)
        String verifiedEmail = (String) session.getAttribute(Define.VERIFIED_EMAIL);
        userService.join(joinDto, verifiedEmail);

        // 가입 후 완료 임시 저장해둔 세션이 이메일 정보 제거
        session.removeAttribute(Define.VERIFIED_EMAIL);
        return "redirect:/login";
    }

    // GET http://localhost:8080/login
    @GetMapping("/login")
    public String loginForm() {
        return "user/login-form";
    }

    // POST http://localhost:8080/login
    // 로그인 처리 (예외적으로 POTS 요청)
    @PostMapping("/login")
    public String login(UserRequest.LoginDto loginDto, HttpSession session, Model model) {
        loginDto.validate();
        SessionUserDto sessionUserDto = userService.login(loginDto);
        session.setAttribute(Define.SESSION_USER, sessionUserDto);
        return "redirect:/";
    }

    // GET http://localhost:8080/user/update
    @GetMapping("/user/update")
    public String updateForm(Model model, HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        UserResponse.updateFormDto userUpdateFormDto = userService.findUpdateForm(sessionUserDto.getId());
        model.addAttribute("user", userUpdateFormDto);
        return "user/update-form";
    }

    // POST http://localhost:8080/user/update
    @PostMapping("/user/update")
    public String update(UserRequest.UpdateDto updateDto, Model model, HttpSession session) {
        updateDto.validate();
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        SessionUserDto updateSessionUserDto = userService.updateById(sessionUserDto.getId(), updateDto);
        // 세션 동기화 처리
        session.setAttribute(Define.SESSION_USER, updateSessionUserDto);
        return "redirect:/user/detail";
    }

    // GET http://localhost:8080/logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
