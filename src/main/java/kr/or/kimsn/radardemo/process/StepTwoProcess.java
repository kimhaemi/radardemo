package kr.or.kimsn.radardemo.process;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import kr.or.kimsn.radardemo.common.DataCommon;
import kr.or.kimsn.radardemo.dto.ReceiveConditionCriteriaDto;
import kr.or.kimsn.radardemo.dto.ReceiveConditionDto;
import kr.or.kimsn.radardemo.dto.ReceiveDataDto;
import kr.or.kimsn.radardemo.dto.StationDto;
import kr.or.kimsn.radardemo.service.QueryService;
import lombok.RequiredArgsConstructor;

// @Slf4j
@Service
@RequiredArgsConstructor
public class StepTwoProcess {
    private final QueryService queryService;

    private List<StationDto> srDto;
    
    @Transactional
    public void stepTwo(){
        String gubunStr = "";
        String dataKindStr = "";
        String dataType = "NQC"; // 데이터 타입
        int srCnt = 0; //site
        String new_recv_condition = "";
        String where_recv_condition = "";
        
        int gubun = Integer.parseInt(DataCommon.getInfoConf("siteInfo", "data_kind"));
        System.out.println("[데몬 구분 int] : " + gubun);
        
        if (gubun == 1)
            gubunStr = "대형";
        if (gubun == 2)
            gubunStr = "소형";
        if (gubun == 3)
            gubunStr = "공항";
        System.out.println("[데몬 구분 str] : " + gubunStr);

        if (gubun == 1)
            dataKindStr = "RDR";
        if (gubun == 2)
            dataKindStr = "SDR";
        if (gubun == 3)
            dataKindStr = "TDWR";
        System.out.println("[데이터 종류 str] : " + dataKindStr);

        String mode = DataCommon.getInfoConf("ipInfo", "mode");
        System.out.println("[mode] : " + mode);

        if (mode.equals("test"))
            srCnt = 1;
        if (!mode.equals("test")) {
            srDto = queryService.getStation(gubun);
            srCnt = srDto.size();
        }

        System.out.println("[radar size] : " + srCnt);

        for(int a=0; a< srCnt; a++){
            String site_cd = "";
            // String siteStr = "";
            if(srCnt == 1) {
                site_cd = "TEST";
                // siteStr = "TEST";
            }
            if(srCnt > 1) { 
                site_cd = srDto.get(a).getSiteCd();
                // siteStr = srDto.get(a).getName_kr(); 
            }

            // 최종상태조회
            ReceiveConditionDto rcDto = queryService.getReceiveCondition(dataKindStr, "NQC", site_cd);
            // System.out.println("[최종상태 조회] : " + rcDto);
            where_recv_condition = rcDto.getRecv_condition();

            // 경고 기준 (횟수 - criterion)
            List<ReceiveConditionCriteriaDto> rccDto = queryService.getReceiveConditionCriteriaList(gubun);
            // System.out.println("[경고 기준] : " + rccDto);
            int filesize_ok = 0;
            int file_ok = 0;
            int network_ok = 0;
            for(ReceiveConditionCriteriaDto rcc : rccDto){
                System.out.println(rcc);
                //복구 - filesize_ok
                if(rcc.getCodedtl().equals("filesize_ok")){
                    List<ReceiveDataDto> rdDto = queryService.getReceiveDataCodedtlList(site_cd, dataKindStr, rcc.getCriterion(), "filesize_ok");
                    filesize_ok = rdDto.size();
                }
                // 복구 - file_ok
                if(rcc.getCodedtl().equals("file_ok")){
                    List<ReceiveDataDto> rdDto = queryService.getReceiveDataCodedtlList(site_cd, dataKindStr, rcc.getCriterion(), "file_ok");
                    file_ok = rdDto.size();
                }

                if(rcc.getCodedtl().equals("network_ok")){
                // 네트워크 복구
                    List<ReceiveDataDto> rdDto = queryService.getReceiveDataCodedtlList(site_cd, dataKindStr, rcc.getCriterion(), "network_ok");
                    network_ok = rdDto.size();
                }

                
            } //end for(ReceiveConditionCriteriaDto rcc : rccDto){

            if(filesize_ok > 0 || file_ok > 0){
                new_recv_condition = "RETR";
            }
            if(network_ok > 0){
                new_recv_condition = "TORE";
            }
            System.out.println("[new_recv_condition] : " + new_recv_condition);
            System.out.println("[where_recv_condition] : " + where_recv_condition);
            System.out.println("[filesize_ok] : " + filesize_ok);
            System.out.println("[file_ok] : " + file_ok);
            System.out.println("[network_ok] : " + network_ok);

            // 최종상태 update
            Integer total = queryService.updateReceiveCondition(new_recv_condition, 0, where_recv_condition, site_cd, dataKindStr, dataType);
            // 이력 update
            if(where_recv_condition.equals("ORDI")) where_recv_condition = "RECV";
            if(where_recv_condition.equals("WARN")) where_recv_condition = "MISS";
            Integer hist = queryService.updateReceiveData(new_recv_condition, site_cd, dataKindStr, dataType, where_recv_condition);

            System.out.println("[total] ::: " + total);
            System.out.println("[hist] ::: " + hist);
        }

    }
}
