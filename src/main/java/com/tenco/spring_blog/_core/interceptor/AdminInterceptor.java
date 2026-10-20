package com.tenco.spring_blog._core.interceptor;

import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.error.UnAuthorizedException;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// 관리자 전용 페이지 보호
@Component
public class AdminInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 확인만 해야 하므로 새로운 세션을 만들면 안됨. false 설정 필수 !
        HttpSession session = request.getSession(false);
        SessionUserDto sessionUserDto = (session == null) ? null : (SessionUserDto) session.getAttribute(Define.SESSION_USER);

        // 1. 로그인 확인은 우리가 만들었던 LoginInterceptor 가 먼저 처리 (하지만 등록 순서가 바뀌면 대 참사가 일어 난다)
        if (sessionUserDto == null) {
            throw new UnAuthorizedException("로그인 먼저 해주세요");
        }
        // 2. 관리자가 아니면 접근 거부
        if (!sessionUserDto.isAdmin()) {
            throw new Exception403("관리자만 접근할 수 있습니다");
        }

        return true;
    }
}
