package kr.or.kimsn.radardemo.dto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import kr.or.kimsn.radardemo.dto.SmsTargetGroupLinkDto;

public interface SmsTargetGroupLinkRepository extends JpaRepository<SmsTargetGroupLinkDto, String>{
}
