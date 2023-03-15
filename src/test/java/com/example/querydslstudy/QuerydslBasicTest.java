package com.example.querydslstudy;

import com.example.querydslstudy.domain.Member;
import com.example.querydslstudy.domain.Team;
import com.example.querydslstudy.dto.MemberDto;
import com.example.querydslstudy.dto.QMemberDto;
import com.example.querydslstudy.repository.MemberJpaRepository;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static com.example.querydslstudy.domain.QMember.member;
import static com.example.querydslstudy.domain.QTeam.team;
import static com.querydsl.core.types.dsl.Expressions.asString;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    JPAQueryFactory queryFactory;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @BeforeEach
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

    }

    @Test
    public void 집합함수() {
        Tuple tuple = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetchOne();

        Long count = tuple.get(member.count());
        Integer sum = tuple.get(member.age.sum());
        Double avg = tuple.get(member.age.avg());
        Integer max = tuple.get(member.age.max());
        Integer min = tuple.get(member.age.min());

    }

    @Test
    public void startJPQL() {
        // member1을 찾아라
        String qlString = "select m from Member m where m.username = :username";
        Member findMember = entityManager.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() {
        // JPAQueryFactory를 필드로 제공하면 동시성 문제는 어떻게 될까? 동시성 문제는 JPAQueryFactory를 생성할 때 제공하는 EntityManager(em)에 달려있다.
        // 스프링 프레임워크는 여러 쓰레드에서 동시에 같은 EntityManager에 접근해도, 트랜잭션 마다 별도의 영속성 컨텍스트를 제공하기 때문에, 동시성 문제는 걱정하지 않아도 된다.

        // member1을 찾아라
        // 컴파일시 에러를 잡을 수 있다.
        // preparedstatement 파라미터 바인딩 방식을 이용, 성능에 유리하다.
        // 자동완성을 통한 편의성 높음
        // QMember m = new QMember("m"); // 별칭 사용 (같은 테이블을 조인해야 할때)
        // QMember m = QMember.member; // Q객체의 기본 인스턴스 사용

        // 기본 인스턴스를 static import와 함께 사용
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"), member.age.eq(10))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch() {
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        Member fetchOne = queryFactory
                .selectFrom(member)
                .fetchOne();

        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();

        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .fetchResults();

        List<Member> results = queryResults.getResults();
        long total = queryResults.getTotal();

        long fetchCount = queryFactory
                .selectFrom(member)
                .fetchCount();

    }

    @Test
    public void join() {

        List<Member> members = queryFactory.selectFrom(member)
                .join(member.team, team)
                .fetch();

        for (Member m : members) {
            System.out.println(m.getTeam().getId());
        }
    }

    /*
     * 세타조인
     * 회원의 이름이 팀 이름과 같은 회원 조회
     * */
    @Test
    public void theta_join() {
        entityManager.persist(Member.builder().username("teamA").build());
        entityManager.persist(Member.builder().username("teamB").build());

        List<Member> members = queryFactory
                .selectFrom(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(members)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    /*
     * 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * select m,t from Member m left join m.team t on t.name = 'teamA'
     * */
    @Test
    public void join_on_filtering() {
        List<Tuple> tuples = queryFactory
                .select(member, asString("good"))
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : tuples) {
            System.out.println("tuple = " + tuple);
        }
    }

    /*
     * 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인 회원은 모두 가져온다
     * JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
     */
    @Test
    public void join_on_no_relation() {
        entityManager.persist(Member.builder().username("teamA").build());
        entityManager.persist(Member.builder().username("teamB").build());

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team) // 엔티티 하나만 들어감
                .on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void join_test() {
        List<Member> list = entityManager.createQuery("select m from Member m join m.team", Member.class)
                .getResultList();
        for (Member m : list) {
            m.getTeam().getName(); // 지연로딩 호출
        }
    }

    @Test
    public void fetch_join_test() {
        List<Member> list = entityManager.createQuery("select m from Member m join fetch m.team", Member.class)
                .getResultList();
        for (Member m : list) {
            System.out.println("팀이름 = " + m.getTeam().getName());
        }
    }

    @Test
    public void bulkUpdate() {

        Member findMember = entityManager.createQuery("select m from Member m where m.id = 1L", Member.class)
                .getSingleResult();

        queryFactory
                .update(member)
                .set(member.age, 0)
                .execute();

        Member findMember2 = queryFactory
                .selectFrom(member)
                .where(member.id.eq(1L))
                .fetchOne();

        System.out.println(findMember2.getAge()); // 10 출력
        // 영속성 컨텍스트 초기화
        entityManager.flush();
        entityManager.clear();

        Member findMember3 = queryFactory
                .selectFrom(member)
                .where(member.id.eq(1L))
                .fetchOne();


        System.out.println(findMember3.getAge()); // 0 출력
    }

    @Test
    public void proxy_test() {

        String jpql = "select m from Member m "
                + "where (select count(t) from m.team t where t.name = m.username) > 0";
        entityManager.createQuery(jpql, Member.class)
                .getResultList();
    }


    @Test
    public void findDtoByJPQL() {
        List<MemberDto> resultList = entityManager.createQuery("select new com.example.querydslstudy.dto.MemberDto(m.username,m.age) from Member m", MemberDto.class)
                .getResultList();

        System.out.println("resultList = " + resultList);

    }

    @Test
    public void findDtoBySetter() {
        List<MemberDto> dtoList = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .where()
                .fetch();
        System.out.println("dtoList = " + dtoList);
    }


}
