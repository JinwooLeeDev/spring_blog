package com.tenco.spring_blog.board;

import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * 게시글 응답 DTO
 * <p>
 * - 서비스(트랜잭션 안)에서 엔티티를 DTO로 변환해서 반환한다.
 * - 뷰는 엔티티가 아닌 DTO 만 사용하므로 OSIV 를 꺼도 지연 로딩 오류가 나지 않는다.
 * - 화면에 필요한 값만 담으므로 계층간에 결합도도 낮아진다.
 */
public class BoardResponse {

    // 게시글 목록 + 페이지 정보 응답 DTO
    @Data
    public static class PageDto {
        private List<ListDto> content; // 현재 페이지의 게시글 목록
        private int size;              // 페이지 크기
        private int currentPage;       // 현재 페이지 (1부터 시작)
        private int totalPages;        // 전체 페이지 수
        private long totalElements;     // 전체 게시글 수
        private boolean hasPrevious;   // 이전 페이지 있음 여부
        private boolean hasNext;       // 다음 페이지 있음 여부
        private List<PageLink> pagesLinks;    // 화면에 표시할 페이지 번호 목록
        private int previousPage;
        private int nextPage;

        public PageDto(Page<Board> page) {
            this.content = page.getContent().stream()
                    .map(ListDto::new)
                    .toList();
            this.size = page.getSize();
            this.currentPage = page.getNumber() + 1;    // 0부터 시작 + 1 (사용자에게는 1로 표시)
            this.previousPage = currentPage - 1;
            this.nextPage = currentPage + 1;
            this.totalPages = page.getTotalPages();
            this.totalElements = page.getTotalElements();
            this.hasPrevious = page.hasPrevious();
            this.hasNext = page.hasNext();
            this.pagesLinks = makePageLinks(currentPage, totalPages);
        }

        // 편의 메서드 : 현재 페이지를 가운데 두고 앞 뒤 2개씩, 최대 5개의 페이지 링크 생성
        private List<PageLink> makePageLinks(int currentPage, int totalPages) {
            List<PageLink> links = new ArrayList<>();

            int startPage = Math.max(1, currentPage - 2); // 1보다 작아지지 않게 하는 코드
            int endPage = Math.min(totalPages, currentPage + 2);

            for (int i = startPage; i <= endPage; i++) {
                links.add(new PageLink(i, i == currentPage));
            }
            return links;
        }
    } // end of PageDto

    // 페이지 번호 버튼 하나의 정보
    @Data
    @AllArgsConstructor
    public static class PageLink {
        private int number; // 표시할 페이지 번호 (1부터)
        private boolean active; // 현재 페이지 여부 (강조 표시용)
    }


    @Data
    public static class ListDto {
        private Long id;
        private String title;
        private String username;
        private String createdAt;

        public ListDto(Board board) {
            this.id = board.getId();
            this.title = board.getTitle();
            // JOIN FETCH로 이미 가져온 user 사용할 예정(추가 쿼리 발생 없음)
            if (board.getUser() != null) {
                this.username = board.getUser().getUsername();
            }
            this.createdAt = board.getTime();
        }
    }


    // 게시글 상세 응답 DTO
    @Data
    public static class DetailDto {
        private Long id;
        private String title;
        private String content;
        private Long userId;
        private String username;
        private String createdAt;

        public DetailDto(Board board) {
            this.id = board.getId();
            this.title = board.getTitle();
            this.content = board.getContent();
            // 지연 로딩 오류 방지를 위한 방어적 코드 작성
            if (board.getUser() != null) {
                this.userId = board.getUser().getId();
                this.username = board.getUser().getUsername();
            }
            if (board.getCreatedAt() != null) {
                this.createdAt = MyDateUtil.timestampFormat(board.getCreatedAt());
            }
        }
    }

    // 게시글 수정 화면 응답 DTO
    public static class UpdateFormDto {
        private Long id;
        private String title;
        private String content;

        public UpdateFormDto(Board board) {
            this.id = board.getId();
            this.title = board.getTitle();
            this.content = board.getContent();
        }
    }
}
