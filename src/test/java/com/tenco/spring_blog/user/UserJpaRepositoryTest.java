package com.tenco.spring_blog.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserJpaRepositoryTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private EntityManager entityManager;

    // 테스트마다 고유한 사용자 데이터를 직접 저장한다.
    private User 사용자저장(String username, String email) {
        User user = User.builder()
                .username(username) // 확인 필요: User에 username 빌더가 있다고 가정
                .email(email)       // 확인 필요: User에 email 빌더가 있다고 가정
                .password("encoded-password") // 확인 필요: password 필드와 빌더가 있다고 가정
                .build();

        return userJpaRepository.saveAndFlush(user);
    }

    @Test
    void findByUsernameWithRoles_사용자가존재하는_테스트() {
        // given: 조회할 사용자를 DB에 저장한다.
        User savedUser = 사용자저장("user1", "user1@test.com");

        // when: 사용자명으로 역할과 함께 조회한다.
        Optional<User> result =
                userJpaRepository.findByUsernameWithRoles("user1");

        // then: 조회된 사용자의 ID가 저장한 사용자의 ID와 같은지 확인한다.
        assertThat(result)
                .isPresent()
                .get()
                .extracting(User::getId)
                .isEqualTo(savedUser.getId()); // 확인 필요: User에 getId()가 있다고 가정
    }

    @Test
    void findByUsernameWithRoles_사용자가없는_테스트() {
        // given: 조회할 사용자명을 가진 데이터를 저장하지 않는다.

        // when: 존재하지 않는 사용자명을 조회한다.
        Optional<User> result =
                userJpaRepository.findByUsernameWithRoles("unknown-user");

        // then: 조회 결과가 비어 있는지 확인한다.
        assertThat(result).isEmpty();
    }

    @Test
    void findByUsernameWithRoles_역할함께로딩_테스트() {
        // given: 조회할 사용자를 저장하고 영속성 컨텍스트를 비운다.
        사용자저장("user2", "user2@test.com");
        entityManager.flush();
        entityManager.clear();

        // when: JOIN FETCH 쿼리로 사용자를 다시 조회한다.
        User foundUser = userJpaRepository
                .findByUsernameWithRoles("user2")
                .orElseThrow();

        // then: JPA가 roles 연관관계를 로딩한 상태인지 확인한다.
        PersistenceUnitUtil persistenceUnitUtil =
                entityManager.getEntityManagerFactory()
                        .getPersistenceUnitUtil();

        assertThat(persistenceUnitUtil.isLoaded(foundUser, "roles"))
                .isTrue();
    }

    @Test
    void findByUsername_사용자가존재하는_테스트() {
        // given: 특정 사용자명을 가진 사용자를 저장한다.
        User savedUser = 사용자저장("user3", "user3@test.com");

        // when: 저장한 사용자명으로 조회한다.
        Optional<User> result =
                userJpaRepository.findByUsername("user3");

        // then: 저장한 사용자와 동일한 ID인지 확인한다.
        assertThat(result)
                .isPresent()
                .get()
                .extracting(User::getId)
                .isEqualTo(savedUser.getId()); // 확인 필요: User에 getId()가 있다고 가정
    }

    @Test
    void findByUsername_사용자가없는_테스트() {
        // given: 조회할 사용자명을 가진 데이터를 저장하지 않는다.

        // when: 등록되지 않은 사용자명으로 조회한다.
        Optional<User> result =
                userJpaRepository.findByUsername("unknown-user");

        // then: 조회 결과가 비어 있는지 확인한다.
        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_이메일이일치하는_테스트() {
        // given: 조회할 이메일을 가진 사용자를 저장한다.
        User savedUser = 사용자저장("user4", "user4@test.com");

        // when: 저장한 이메일로 사용자를 조회한다.
        Optional<User> result =
                userJpaRepository.findByEmail("user4@test.com");

        // then: 저장한 사용자와 동일한 ID인지 확인한다.
        assertThat(result)
                .isPresent()
                .get()
                .extracting(User::getId)
                .isEqualTo(savedUser.getId()); // 확인 필요: User에 getId()가 있다고 가정
    }

    @Test
    void findByEmail_이메일이일치하지않는_테스트() {
        // given: 조회할 이메일을 가진 데이터를 저장하지 않는다.

        // when: 등록되지 않은 이메일로 조회한다.
        Optional<User> result =
                userJpaRepository.findByEmail("unknown@test.com");

        // then: 조회 결과가 비어 있는지 확인한다.
        assertThat(result).isEmpty();
    }
}