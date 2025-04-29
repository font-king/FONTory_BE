package org.fontory.fontorybe.member.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.member.controller.dto.InitMemberInfoRequest;
import org.fontory.fontorybe.member.controller.port.MemberCreationService;
import org.fontory.fontorybe.member.controller.port.MemberLookupService;
import org.fontory.fontorybe.member.controller.port.MemberOnboardService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyJoinedException;
import org.fontory.fontorybe.member.domain.exception.MemberDuplicateNameExistsException;
import org.fontory.fontorybe.member.infrastructure.entity.MemberStatus;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.fontory.fontorybe.provide.domain.Provide;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Builder
@Service
@RequiredArgsConstructor
public class MemberOnboardServiceImpl implements MemberOnboardService {
    private final MemberRepository memberRepository;
    private final MemberLookupService memberLookupService;
    private final MemberCreationService memberCreationService;

    @Override
    @Transactional
    public Member fetchOrCreateMember(Provide p) {
        if (p.getMemberId()==null) {
            return memberCreationService.createDefaultMember(p);
        } else {
            return memberLookupService.getOrThrowById(p.getMemberId());
        }
    }

    @Override
    @Transactional
    public Member initNewMemberInfo(Long requestMemberId, InitMemberInfoRequest initNewMemberInfoRequest) {
        Member targetMember = memberLookupService.getOrThrowById(requestMemberId);
        if (memberLookupService.existsByNickname(initNewMemberInfoRequest.getNickname())) {
            throw new MemberDuplicateNameExistsException();
        } else if (targetMember.getStatus() == MemberStatus.ACTIVATE) {
            throw new MemberAlreadyJoinedException();
        }

        return memberRepository.save(targetMember.initNewMemberInfo(initNewMemberInfoRequest));
    }
}
