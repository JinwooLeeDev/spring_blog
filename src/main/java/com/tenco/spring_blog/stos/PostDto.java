package com.tenco.spring_blog.stos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 서버측에 던질 데이로 사용할 예정
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostDto {

    private Integer userId;
    private String body;
    private String title;
    private Integer id; // 보낼때는 id를 모를 저증 후 응답 시 할당 됨.

    @Builder
    public PostDto(Integer userId, String body, String title) {
        this.userId = userId;
        this.body = body;
        this.title = title;
    }
}
