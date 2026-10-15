package com.tenco.spring_blog;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringBlogApplicationTests {

    public static void main(String[] args) {
        int page = 236246;
        int validSize = Math.max(1, Math.min(50, page));

        System.out.println("출력값 : " + validSize);
    }
}
