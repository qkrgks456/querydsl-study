package com.example.querydslstudy.repository.data;

import com.example.querydslstudy.dto.MemberSearchDto;
import com.example.querydslstudy.dto.MemberTeamDto;
import com.example.querydslstudy.dto.QMemberTeamDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.example.querydslstudy.domain.QMember.member;
import static com.example.querydslstudy.domain.QTeam.team;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
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

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchDto memberSearchDto, Pageable pageable) {
        List<MemberTeamDto> contents = queryFactory
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
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(member.count())
                .from(member)
                .join(member.team, team)
                .where(usernameEq(memberSearchDto.getUsername()),
                        teamNameEq(memberSearchDto.getTeamName()),
                        ageGoe(memberSearchDto.getAgeGoe()),
                        ageLoe(memberSearchDto.getAgeLoe()));

        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne); // 성능 더 좋음
        //  return new PageImpl<>(contents, pageable, count);
    }

    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchDto memberSearchDto, Pageable pageable) {
        return null;
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
