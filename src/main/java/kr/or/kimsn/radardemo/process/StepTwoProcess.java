package kr.or.kimsn.radardemo.process;

import java.util.List;

import org.springframework.stereotype.Service;

import kr.or.kimsn.radardemo.common.DataCommon;
import kr.or.kimsn.radardemo.dto.ReceiveConditionCriteriaDto;
import kr.or.kimsn.radardemo.dto.ReceiveConditionDto;
import kr.or.kimsn.radardemo.dto.ReceiveDataDto;
import kr.or.kimsn.radardemo.dto.StationDto;
import kr.or.kimsn.radardemo.dto.repository.ReceiveConditionCriteriaRepository;
import kr.or.kimsn.radardemo.dto.repository.ReceiveConditionRepository;
import kr.or.kimsn.radardemo.dto.repository.ReceiveDataRepository;
import kr.or.kimsn.radardemo.dto.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StepTwoProcess {

    private final StationRepository stationRepository;

    private final ReceiveConditionRepository receiveConditionRepository; //최종
    private final ReceiveDataRepository receiveDataRepository; //이력
    private final ReceiveConditionCriteriaRepository receiveConditionCriteriaRepository;//경고 기준


    public void stepTwo(){
        String gubunStr = "";
        int srCnt = 0;
        List<StationDto> srDto = null;

        int gubun = Integer.parseInt(DataCommon.getInfoConf("siteInfo", "data_kind"));
        System.out.println("[데몬 구분 int] : " + gubun);
        
        if(gubun == 1) gubunStr = "대형";
        if(gubun == 2) gubunStr = "소형";
        if(gubun == 3) gubunStr = "공항";
        System.out.println("[데몬 구분 str] : " + gubunStr);

        String mode = DataCommon.getInfoConf("ipInfo", "mode");
        System.out.println("[mode] : " + mode);

        if (mode.equals("test")) srCnt = 1;
        if (!mode.equals("test")) {
            srDto = stationRepository.findByGubunOrderBySortOrder(gubun);
            srCnt = srDto.size();
        }

        String dataKindStr = "";

        if(gubun == 1) dataKindStr = "RDR";
        if(gubun == 2) dataKindStr = "SDR";
        if(gubun == 3) dataKindStr = "TDWR";
        
        System.out.println("[gubun] : " + dataKindStr);

        for(int a=0; a< srCnt; a++){
            String site_cd = "";
            if(srCnt == 1) site_cd = "TEST";
            if(srCnt > 1) site_cd = srDto.get(a).getSiteCd();

            site_cd = "BRI";

            // 최종상태조회
            ReceiveConditionDto rcDto = receiveConditionRepository.findByDataKindAndDataTypeAndSite(dataKindStr, "NQC", site_cd);
            System.out.println("[최종이력상태 조회] : " + rcDto);

            // 경고 기준 (횟수 - criterion)
            ReceiveConditionCriteriaDto rccDto = receiveConditionCriteriaRepository.getReceiveConditionCriteriaList(gubun, rcDto.getRecv_condition(), rcDto.getCodedtl());
            
            if(!rcDto.getRecv_condition().equals("ORDI")){
                //이력 조회
                List<ReceiveDataDto> rdDto = receiveDataRepository.getReceiveDataList(dataKindStr, rccDto.getCriterion());
                System.out.println("[이력 조회] : " + rdDto);
                for(int b=0; b<rdDto.size(); b++){
                    System.out.println("site 별 3회 체크");
                }
            }

        }
        
    }
    
}
