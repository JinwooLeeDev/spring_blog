package com.tenco.spring_blog._core.error;

// 401 Unauthorized
// RuntimeException 을 상속하여 언체크 예외로 만듬
public class Exception401 extends RuntimeException{
    // 예외 메세지를 받을 수 있도록 String 파라미터 설계
    public Exception401(String msg) {
        super(msg); // 부모 클래스의 메시지 설정
    }
}
