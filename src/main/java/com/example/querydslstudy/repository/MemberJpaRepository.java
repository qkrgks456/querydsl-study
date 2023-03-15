package com.example.querydslstudy.repository;

import com.example.querydslstudy.domain.Member;
import com.example.querydslstudy.domain.QMember;
import com.example.querydslstudy.domain.QTeam;
import com.example.querydslstudy.dto.MemberSearchDto;
import com.example.querydslstudy.dto.MemberTeamDto;
import com.example.querydslstudy.dto.QMemberTeamDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;

import static com.example.querydslstudy.domain.QMember.member;
import static com.example.querydslstudy.domain.QTeam.team;

@Repository
@RequiredArgsConstructor
public class MemberJpaRepository {

    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;

    public Page<Member> search(Pageable pageable) {
        List<Member> contents = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(member.count())
                .from(member)
                .join(member.team, team);

        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne); // 성능 더 좋음
        //  return new PageImpl<>(contents, pageable, count);
    }


    public List<Member> members() {
        return queryFactory.selectFrom(member)
                .fetch();
    }

    public List<MemberTeamDto> searchByBuilder(MemberSearchDto memberSearchDto) {
        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(memberSearchDto.getUsername())) {
            builder.and(member.username.eq(memberSearchDto.getUsername()));
        }

        if (StringUtils.hasText(memberSearchDto.getTeamName())) {
            builder.and(team.name.eq(memberSearchDto.getTeamName()));
        }

        if (memberSearchDto.getAgeGoe() != null) {
            builder.and(member.age.goe(memberSearchDto.getAgeGoe()));
        }

        if (memberSearchDto.getAgeLoe() != null) {
            builder.and(member.age.loe(memberSearchDto.getAgeLoe()));
        }
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .join(member.team, team)
                .where(builder)
                .fetch();
    }

    public List<MemberTeamDto> search(MemberSearchDto memberSearchDto) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .join(member.team, team)
                .where(usernameEq(memberSearchDto.getUsername()),
                        teamNameEq(memberSearchDto.getTeamName()),
                        ageGoe(memberSearchDto.getAgeGoe()),
                        ageLoe(memberSearchDto.getAgeLoe()))
                .fetch();
    }

    private BooleanExpression teamNameEq(String teamName) {
        return StringUtils.hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression usernameEq(String username) {
        return StringUtils.hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }
}
