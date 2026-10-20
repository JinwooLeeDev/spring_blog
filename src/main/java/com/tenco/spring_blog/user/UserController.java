package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.UnAuthorizedException;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.stos.PostDto;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
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

@RequiredArgsConstructor // DI
@Slf4j
@Controller // IoC (제어의 역전) 싱글톤 패턴으로 관리 됨.
public class UserController {

    private final UserService userService;
    // application.yml 의 변수를 가지고 올 때 사용
    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;
    @Value("${oauth.kakao.redirect-uri}")
    private String redirectUri;
    @Value("${oauth.kakao.client-secret}")
    private String clientSecret;



    @GetMapping("/kakao-login")
    public String kakaoLogin() {
        String url = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + kakaoClientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code";
        return "redirect:" + url;
    }

    // [흐름] 1. 인가 코드 받기 -> 액세스 토큰 요청 -> 사용자 정보 요청 -> 가입 또는 로그인
    @GetMapping("/oauth/kakao")

    public String kakaoCallback(@RequestParam(name = "code", required = false) String code,
                                HttpSession session ) {

        if (code == null) {
            throw new UnAuthorizedException("카카오 로그인이 취소되었습니다");
        }

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<UserResponse.OauthToken> tokenResponse;
        ResponseEntity<UserResponse.KakaoUserInfo> profileResponse;
        try {
            // 1. 받은 인가 코드를 활용해서 토큰 요청 (POST)
            HttpHeaders tokenHeaders = new HttpHeaders();
            // Content-Type: application/x-www-form-urlencoded;charset=utf-8
            tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
            tokenParams.add("grant_type", "authorization_code");
            tokenParams.add("client_id", kakaoClientId);
            tokenParams.add("redirect_uri", redirectUri);
            tokenParams.add("code", code);
            // 클라이언트 시크릿 활성화시 반드시 포함
            tokenParams.add("client_secret", clientSecret);

            // 바디 + 헤더
            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity(tokenParams, tokenHeaders);

            // 주소, method, HTTP 메세지, 응답 타입
            tokenResponse = restTemplate.exchange(
                    "https://kauth.kakao.com/oauth/token",
                    HttpMethod.POST,
                    tokenRequest,
                    UserResponse.OauthToken.class
            );

            // 액세스 토큰으로 사용자 정보 요청(카카오 서버)
            // 헤더 만들기
            HttpHeaders profileHeaders = new HttpHeaders();
            profileHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            profileHeaders.setBearerAuth(tokenResponse.getBody().getAccessToken());

            // 헤더 + 엔티티 --> HTTP 요청 완성
            HttpEntity profileRequest = new HttpEntity(profileHeaders);

            profileResponse = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    profileRequest,
                    UserResponse.KakaoUserInfo.class
            );

            UserResponse.KakaoUserInfo kakaoUserInfo = profileResponse.getBody();

            // 내부 로직 - 최초 소셜 로그인 사용자라면 자동 회원 가입 (필요 정보가 더 있다면 추가 기입)
            //           - 이미 회원 가입이 되어 있는 사용자라면 로그인 처리
            SessionUserDto sessionUserDto = userService.kakaoLogin(kakaoUserInfo);
            session.setAttribute(Define.SESSION_USER, sessionUserDto);

            return "redirect:/";

        } catch (HttpClientErrorException e) {
            // 카카오가 4xx 를 응답한경우
            log.error("카카오 API 오류 : {}", e.getResponseBodyAsString());
            throw new UnAuthorizedException("카카오 로그인에 실패했습니다");
        }
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
    public String join(UserRequest.JoinDto joinDto, Model model) {
        joinDto.validate();
        userService.join(joinDto);
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
