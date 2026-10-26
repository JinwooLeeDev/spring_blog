package com.tenco.spring_blog.purchase;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog.board.Board;
import com.tenco.spring_blog.board.BoardJpaRepository;
import com.tenco.spring_blog.user.User;
import com.tenco.spring_blog.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PurchaseService {

    private final BoardJpaRepository boardJpaRepository;
    private final PurchaseJpaRepository purchaseJpaRepository;
    private final UserJpaRepository userJpaRepository;

    // 유료 게시글 구매
    // 포인트 차감 + 구매 내역 저장을 하나의 트랜잭션을 처리 (하나라도 실패하면 모두 롤백)
    @Transactional
    public void purchase(Long boardId, Long userId) {
        // 1. 게시글 조회
        Board board = boardJpaRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 없습니다"));

        // 2. 유료 게시글인지 확인
        if (!board.isPremium()) {
            throw new Exception400("유료 게시글이 아닙니다");
        }

        // 3. 작성자는 자기 글을 구매할 수 없음(이미 열람 가능)
        if (board.isOwner(userId)) {
            throw new Exception400("자신이 작성한 게시글을 구매할 수 없습니다");
        }

        // 4. 이미 구매했는지 확인
        if (purchaseJpaRepository.existsByUserIdAndBoardId(userId, boardId)) {
            throw new Exception400("이미 구매한 게시글입니다");
        }
        // 5. 구매자 조회 후 포인트 차감 (포인트 부족할 수 있임. 포인트 차감 처리 해야 함) --> 롤백
        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다"));
        user.deductPoint(Board.PREMIUM_PRICE); // 일단 하드 코딩으로 고정

        // 6. 구매 내역 저장
        Purchase purchase = Purchase.builder()
                .user(user)
                .board(board)
                .price(Board.PREMIUM_PRICE)
                .build();

        purchaseJpaRepository.save(purchase);
    }

}






