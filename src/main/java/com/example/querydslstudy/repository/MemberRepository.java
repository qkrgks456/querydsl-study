package com.example.querydslstudy.repository;

import com.example.querydslstudy.domain.Member;
import com.example.querydslstudy.repository.data.MemberRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    Member findByUsername(String username);
}
