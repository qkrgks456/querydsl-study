package com.example.querydslstudy;

import com.example.querydslstudy.domain.Hello;
import com.example.querydslstudy.domain.QHello;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@SpringBootTest
@Transactional
@Commit
class QuerydslStudyApplicationTests {

    @Autowired
    EntityManager entityManager;

    @Test
    void contextLoads() {
        // 엔티티 영속
        Hello hello = new Hello();
        entityManager.persist(hello);

        // 쿼리팩토리 생성
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        // Q타입 객체 생성
        QHello qHello = new QHello("h");

        // 쿼리팩토리로 쿼리 생성
        Hello result = queryFactory.selectFrom(qHello)
                .fetchOne();

        // 같은지
        Assertions.assertThat(result).isEqualTo(hello);
    }

}
