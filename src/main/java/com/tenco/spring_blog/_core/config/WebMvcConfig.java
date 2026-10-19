package com.tenco.spring_blog._core.config;

import com.tenco.spring_blog._core.interceptor.LoginInterceptor;
import com.tenco.spring_blog._core.interceptor.SessionUserModelInterceptor;
import com.tenco.spring_blog._core.util.FileUtil;
import com.tenco.spring_blog.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

// IoC
@RequiredArgsConstructor
@Configuration // 스프링 설정 크래스임을 표시 (빈으로도 등록 됨)
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    private final SessionUserModelInterceptor SessionUserModelInterceptor;

    // 내가 정의한 인터셉터를 설정 클래스 등록할 수 있다.
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(SessionUserModelInterceptor)
                .addPathPatterns("/**");
        registry.addInterceptor(loginInterceptor)
                // 인터셉터가 동작할 URL 패턴을 지정
                .addPathPatterns("/user/**", "/board/**", "/reply/**")
                // 인터셉터에서 제외할 URL 패턴을 지정
                .excludePathPatterns("/board/list", "/board/{id:\\d+}");
        // \\d+ 는 정규표현식으로 1개 이상의 숫자를 의미한다.
        // 예: /board/1, /board/123 (상세보기)은 로그인 없이도 접근 가능
        //  /board/1/update 처럼 뒤에 경로가 더 붙으면 제외 대상이 아니게 된다.

    }

    // 업로드 폴더의 파일일 /images/** 주소로 제공
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드 폴더 경로를 file:///.... 형식의 주소로 변환(운영체제에 맞게 자동 변환)
        String location = Paths.get(FileUtil.UPLOAD_DIR).toUri().toString();
        // 윈도우 기준 : C:\Users\사용자명\blog_uploads\

        // 폴더 위치는 반드시 / 로 끝나야 그 안의 파일을 찾을 수 있임
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/images/**")
                .addResourceLocations(location);
    }

}
