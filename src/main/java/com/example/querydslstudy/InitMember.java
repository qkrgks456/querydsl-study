package com.example.querydslstudy;

import com.example.querydslstudy.domain.Member;
import com.example.querydslstudy.domain.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Profile("local")
@Component
@RequiredArgsConstructor
public class InitMember {

    private final InitMemberService initMemberService;

    @PostConstruct
    public void init() {
        initMemberService.init();
    }

    @Component
    static class InitMemberService {
        @PersistenceContext
        private EntityManager entityManager;

        @Transactional
        public void init() {
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

            for (int i = 0; i < 100; i++) {
                Team selectedTeam = i % 2 == 0 ? teamA : teamB;
                Member member = Member.builder()
                        .username("member" + i)
                        .age(i)
                        .team(selectedTeam)
                        .build();
                entityManager.persist(member);
            }
        }
    }
}
