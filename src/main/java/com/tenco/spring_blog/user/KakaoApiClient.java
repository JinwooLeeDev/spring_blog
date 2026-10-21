package com.tenco.spring_blog.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

// 카카오 서버와 HTTP 통신만 담당하는 클래스
@Slf4j
@Component // IoC (싱글톤)
public class KakaoApiClient {

    // application.yml 의 변수를 가지고 올 때 사용
    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;
    @Value("${oauth.kakao.redirect-uri}")
    private String redirectUri;
    @Value("${oauth.kakao.client-secret}")
    private String clientSecret;

    private static String AUTHORIZE_URL = "https://kauth.kakao.com/oauth/authorize";
    private static String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static String PROFILE_URL = "https://kapi.kakao.com/v2/user/me";
    // 한 번 만들면 여러 요청에서 함께 써도 안전하게 설계 되어 하나만 만들어 재사용 한다.
    private final RestTemplate restTemplate = new RestTemplate();

    // 1. 카카오 로그인 화면 주소 만들기 (로그인 + 동의 항목 화면)
    public String getAuthorizeUrl() {
       return AUTHORIZE_URL
                + "?client_id=" + kakaoClientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code";
    }

    // 2. 인가 코드로 액세스 토큰 요청 (POST, 폼 데이터)
    public UserResponse.OauthToken requestAccessToken(String code) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);
        // 클라이언트 시크릿 활성화시 반드시 포함
        params.add("client_secret", clientSecret);

        // 바디 + 헤더
        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity(params, headers);

        // 주소, method, HTTP 메세지, 응답 타입
        ResponseEntity<UserResponse.OauthToken> response = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                tokenRequest,
                UserResponse.OauthToken.class
        );
        log.info("카카오 액세스 토큰 발급 완료"); // 토큰 값은 로그에 남기지 않는다.
        return  response.getBody();
    }

    // 3. 액세스 토큰으로 사용자 정보 요청 (GET, 인증 헤더)
    public UserResponse.KakaoUserInfo requestProfile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity request = new HttpEntity(headers);

        ResponseEntity<UserResponse.KakaoUserInfo> response = restTemplate.exchange(
                PROFILE_URL,
                HttpMethod.GET,
                request,
                UserResponse.KakaoUserInfo.class
        );
        return response.getBody();
    }

}






