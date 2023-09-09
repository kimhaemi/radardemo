package kr.or.kimsn.radardemo.dto.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.or.kimsn.radardemo.dto.SmsSendPatternDto;
import kr.or.kimsn.radardemo.dto.pkColumn.SmsSendPatternPk;

public interface SmsSendPatternRepository extends JpaRepository<SmsSendPatternDto, SmsSendPatternPk>{
    List<SmsSendPatternDto> findAll();

    List<SmsSendPatternDto> findByOrderByCodeAscModeDesc();

    SmsSendPatternDto findByActivationAndStatusAndModeAndCodeAndCodedtl(int activation, int status, String mode, String code, String coddtl);

    @Query(
        nativeQuery = true,
        value=
        "update sms_send_pattern set \n" +
		"  pattern = :pattern, \n" +
		"  activation = :activation \n" +
        "where 1=1  \n" +
        "  and code = :code \n" +
        "  and codedtl = :codedtl \n" +
        "  and mode = :mode \n"
    )
    @Transactional
    @Modifying
    // 문자 메시지 패턴 일괄 수정
    Integer setSmsSetMsgModify(
        @Param("pattern") String pattern,
        @Param("activation") String activation,
        @Param("code") String code,
        @Param("mode") String mode,
        @Param("codedtl") String codedtl
    );
}
