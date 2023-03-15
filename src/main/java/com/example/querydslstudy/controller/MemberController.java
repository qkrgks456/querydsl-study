package com.example.querydslstudy.controller;

import com.example.querydslstudy.dto.MemberSearchDto;
import com.example.querydslstudy.dto.MemberTeamDto;
import com.example.querydslstudy.repository.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> members(MemberSearchDto memberSearchDto) {
        return memberJpaRepository.search(memberSearchDto);
    }
}
