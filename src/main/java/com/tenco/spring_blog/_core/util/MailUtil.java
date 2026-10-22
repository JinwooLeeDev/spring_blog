package com.tenco.spring_blog._core.util;

import java.security.SecureRandom;

public class MailUtil {
    // 시큐리티 - 예측하기 여러운 난수 생성기 (보안 용도)
    private static final SecureRandom RANDOM = new SecureRandom();

    // 6자라이 인증번호 생성 (100000 ~ 999999)
    public static String generateCode() {
         int code = 100000 + RANDOM.nextInt(900000);  // 0 ~ 899999
        return String.valueOf(code);
    }
}
