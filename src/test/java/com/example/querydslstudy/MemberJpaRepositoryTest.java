package com.example.querydslstudy;

import com.example.querydslstudy.domain.Member;
import com.example.querydslstudy.domain.Team;
import com.example.querydslstudy.dto.MemberSearchDto;
import com.example.querydslstudy.dto.MemberTeamDto;
import com.example.querydslstudy.repository.MemberJpaRepository;
import com.example.querydslstudy.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class MemberJpaRepositoryTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    public void basic_test() {
        memberJpaRepository.members();
    }

    @Test
    public void searchTest() {
        Team teamA = Team.builder()
                .color("red")
                .name("teamA")
                .build();
        Team teamB = Team.builder()
                .color("blue")
                .name("teamB")
                .build();
        entityManager.persist(teamA);
        entityManager.persist(teamB);

        Member member1 = Member.builder()
                .username("member1")
                .age(10)
                .team(teamA)
                .build();
        Member member2 = Member.builder()
                .username("member2")
                .age(20)
                .team(teamA)
                .build();
        Member member3 = Member.builder()
                .username("member3")
                .age(30)
                .team(teamB)
                .build();
        Member member4 = Member.builder()
                .username("member4")
                .age(40)
                .team(teamB)
                .build();

        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(member3);
        entityManager.persist(member4);

        entityManager.flush();
        entityManager.clear();

        MemberSearchDto memberSearchDto = new MemberSearchDto();
        memberSearchDto.setAgeGoe(35);
        memberSearchDto.setAgeLoe(40);
        memberSearchDto.setTeamName("teamB");

        List<MemberTeamDto> list = memberRepository.search(memberSearchDto);

        assertThat(list).extracting("username").containsExactly("member4");
    }

    @Test
    public void searchPageTest() {
        Team teamA = Team.builder()
                .color("red")
                .name("teamA")
                .build();
        Team teamB = Team.builder()
                .color("blue")
                .name("teamB")
                .build();
        entityManager.persist(teamA);
        entityManager.persist(teamB);

        Member member1 = Member.builder()
                .username("member1")
                .age(10)
                .team(teamA)
                .build();
        Member member2 = Member.builder()
                .username("member2")
                .age(20)
                .team(teamA)
                .build();
        Member member3 = Member.builder()
                .username("member3")
                .age(30)
                .team(teamB)
                .build();
        Member member4 = Member.builder()
                .username("member4")
                .age(40)
                .team(teamB)
                .build();

        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(member3);
        entityManager.persist(member4);

        entityManager.flush();
        entityManager.clear();

        MemberSearchDto memberSearchDto = new MemberSearchDto();

        PageRequest pageRequest = PageRequest.of(0, 3);

        Page<MemberTeamDto> list = memberRepository.searchPageSimple(memberSearchDto, pageRequest);



        assertThat(list.getSize()).isEqualTo(3);
        assertThat(list.getContent()).extracting("username").containsExactly("member1", "member2", "member3");
    }
}
