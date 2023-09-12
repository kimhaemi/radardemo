package kr.or.kimsn.radardemo.dto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import kr.or.kimsn.radardemo.dto.ReceiveConditionDto;

public interface ReceiveConditionRepository extends JpaRepository<ReceiveConditionDto, String> {

    List<ReceiveConditionDto> findByDataTypeOrderBySite(String data_type);

    List<ReceiveConditionDto> findBySiteAndDataType(String site, String data_type);

    ReceiveConditionDto findByDataKindAndDataTypeAndSite(String data_kind, String Data_type, String Site);

    @Query(
        nativeQuery = true,
        value=
        "update watchdog.receive_condition set\n"+
		"  sms_send = :sms_send \n"+
	    "where 1=1 \n"+
        "  and recv_condition = :recv_condition \n"+
		"  and site = :site \n"+
        "  and data_kind = :dataKindStr \n"+
		"  and data_type = :dataType \n"
    )
    @Transactional
    @Modifying
    // 지점별 SMS 발송 여부 update
    Integer updateReceiveCondition(
        @Param("sms_send") int sms_send, 
        @Param("recv_condition") String recv_condition, 
        @Param("site") String site, 
        @Param("dataKindStr") String dataKindStr, 
        @Param("dataType") String dataType
    );

}
