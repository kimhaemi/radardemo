package kr.or.kimsn.radardemo.dto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import kr.or.kimsn.radardemo.dto.ReceiveDataDto;
import kr.or.kimsn.radardemo.dto.pkColumn.ReceiveDataPk;

public interface ReceiveDataRepository extends JpaRepository<ReceiveDataDto, ReceiveDataPk> {

    @Query(
        nativeQuery = true,
        value=
        "select \n" +
        "    data_kind,  \n" +
        "    site,  \n" +
        "    data_type,  \n" +
        "    data_time, \n" + 
        "    data_kst,  \n" +
        "    recv_time,  \n" +
        "    recv_condition,  \n" +
        "    recv_condition_check_time,  \n" +
        "    file_name,  \n" +
        "    file_size,  \n" +
        "    codedtl \n" + 
        "from watchdog.receive_data \n" +
        "where 1=1 \n" +
        "  and site = :site \n" +
        "  and data_kind  = :data_kind \n" +
        "  and data_type  = 'NQC' \n" +
        "order by data_kind, site, data_type, data_kst desc \n"+
        "limit :count \n"
    )
    // 지점별 과거자료 검색
    List<ReceiveDataDto> getReceiveDataList(
        @Param("site") String site,
        @Param("data_kind") String data_kind,
        @Param("count") int count
    );

    @Query(
        nativeQuery = true,
        value=
        "select \n" +
        "    data_kind,  \n" +
        "    site,  \n" +
        "    data_type,  \n" +
        "    data_time, \n" + 
        "    data_kst,  \n" +
        "    recv_time,  \n" +
        "    recv_condition,  \n" +
        "    recv_condition_check_time,  \n" +
        "    file_name,  \n" +
        "    file_size,  \n" +
        "    codedtl \n" + 
        "from watchdog.receive_data \n" +
        "where 1=1 \n" +
        "  and site = :site \n" +
        "  and data_kind  = :data_kind \n" +
        "  and data_type  = 'NQC' \n" +
        "  and codedtl  = :codedtl \n" +
        "order by data_kind, site, data_type, data_kst desc \n"+
        "limit :count \n"
    )
    // 지점별 과거자료 검색
    List<ReceiveDataDto> getReceiveDataCodedtlList(
        @Param("site") String site,
        @Param("data_kind") String data_kind,
        @Param("count") int count,
        @Param("codedtl") String codedtl
        // @Param("data_type") String data_type,
        // @Param("dateStart") String dateStart,
        // @Param("dateClose") String dateClose
    );

    @Query(
        nativeQuery = true,
        value=
        "update watchdog.receive_data set  \n" +
        "  recv_condition = :new_recv_condition \n" +
        // "  recv_condition_check_time = now() \n" +
        "where 1=1  \n" +
        "  and site =:site -- param \n" +
        "  and data_kind  = :dataKindStr -- param \n" +
        "  and data_type  = :dataType -- param \n" +
        "  and recv_condition = :where_recv_condition -- param \n"
    )
    @Transactional
    @Modifying
    // 결과 이력 update
    Integer updateReceiveData(
        @Param("new_recv_condition") String new_recv_condition,
        @Param("site") String site, 
        @Param("dataKindStr") String dataKindStr, 
        @Param("dataType") String dataType,
        @Param("where_recv_condition") String where_recv_condition

    );


}
