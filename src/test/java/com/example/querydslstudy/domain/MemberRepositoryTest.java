package com.example.querydslstudy.domain;

import com.example.querydslstudy.repository.MemberJpaRepository;
import com.example.querydslstudy.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@SpringBootTest
@Transactional
public class MemberRepositoryTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    MemberRepository memberRepository;

    @Test
    public void basic_test() {
        Member member = memberRepository.findByUsername("member1");
    }
}
