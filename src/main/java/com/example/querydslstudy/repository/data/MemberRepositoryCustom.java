package com.example.querydslstudy.repository.data;

import com.example.querydslstudy.dto.MemberSearchDto;
import com.example.querydslstudy.dto.MemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


import java.util.List;

public interface MemberRepositoryCustom {

    List<MemberTeamDto> search(MemberSearchDto memberSearchDto);

    Page<MemberTeamDto> searchPageSimple(MemberSearchDto memberSearchDto, Pageable pageable);

    Page<MemberTeamDto> searchPageComplex(MemberSearchDto memberSearchDto, Pageable pageable);


}
