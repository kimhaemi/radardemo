package kr.or.kimsn.radardemo.dto.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.or.kimsn.radardemo.dto.SmsTargetMemberLinkDto;
import kr.or.kimsn.radardemo.dto.pkColumn.SmsTargetMemberLinkPk;

public interface SmsTargetMemberLinkRepository extends JpaRepository<SmsTargetMemberLinkDto, SmsTargetMemberLinkPk>{

}
