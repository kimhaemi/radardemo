package kr.or.kimsn.radardemo.process;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import kr.or.kimsn.radardemo.common.DataCommon;
import kr.or.kimsn.radardemo.common.FormatDateUtil;
import kr.or.kimsn.radardemo.dto.ReceiveConditionCriteriaDto;
import kr.or.kimsn.radardemo.dto.ReceiveConditionDto;
import kr.or.kimsn.radardemo.dto.ReceiveDataDto;
import kr.or.kimsn.radardemo.dto.SmsSendPatternDto;
import kr.or.kimsn.radardemo.dto.StationDto;
import kr.or.kimsn.radardemo.dto.repository.ReceiveConditionCriteriaRepository;
import kr.or.kimsn.radardemo.dto.repository.ReceiveConditionRepository;
import kr.or.kimsn.radardemo.dto.repository.ReceiveDataRepository;
import kr.or.kimsn.radardemo.dto.repository.SmsSendPatternRepository;
import kr.or.kimsn.radardemo.dto.repository.SmsSendRepository;
import kr.or.kimsn.radardemo.dto.repository.StationRepository;
import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// @Slf4j
@Service
@RequiredArgsConstructor
public class StepTwoProcess {

    private final StationRepository stationRepository;

    private final ReceiveConditionRepository receiveConditionRepository; //최종
    private final ReceiveDataRepository receiveDataRepository; //이력
    private final ReceiveConditionCriteriaRepository receiveConditionCriteriaRepository;//경고 기준
    private final SmsSendPatternRepository smsSendPatternRepository;//문자메시지 패턴

    private final SmsSendRepository smsSendRepository; //문자 메시지 전송(app_send_data, app_send_contents)

    private List<StationDto> srDto;
    
    @Transactional
    public void stepTwo(){
        String gubunStr = "";
        int srCnt = 0;

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

            String code = rcDto.getRecv_condition();
            String codedtl = rcDto.getCodedtl();

            // 경고 기준 (횟수 - criterion)
            ReceiveConditionCriteriaDto rccDto = receiveConditionCriteriaRepository.getReceiveConditionCriteriaList(gubun, code, codedtl);
            System.out.println("[site 별 "+rccDto.getCriterion()+"회 체크]");

            //문자메시지 패턴
            SmsSendPatternDto smsSendPatternDto = smsSendPatternRepository.findByActivationAndStatusAndModeAndCodeAndCodedtl(1, 1, "RUN", code, codedtl);

            String dateTime = FormatDateUtil.formatDate("yyyy-MM-dd HH:mm:ss", new Date());
            
            System.out.println("[date Time] : " + dateTime);

            String smsPettern = smsSendPatternDto.getPattern();
            smsPettern = smsPettern.replace("%SITE%", site_cd).replace("TIME", dateTime);

            System.out.println("[문자메시지 패턴] : " + smsPettern);
            
            if(!rcDto.getRecv_condition().equals("ORDI")){
                //이력 조회
                List<ReceiveDataDto> rdDto = receiveDataRepository.getReceiveDataList(dataKindStr, rccDto.getCriterion());
                System.out.println("[이력 조회] : " + rdDto);
                int cnt = 0; // 최종이력상태와 결과 값 다른것
                for(ReceiveDataDto dto : rdDto){
                    if(!dto.getCodedtl().equals(rcDto.getCodedtl())){
                        cnt++;
                    }
                }
                System.out.println("[cnt] : " + cnt);

                if(rccDto.getCriterion() == cnt){
                    //site 수신그룹 담당자에게 문자 전송
                    // select DISTINCT stm.name, stm.phone_num  from sms_target_group stg 
                    // left outer join sms_target_group_link stgl
                    // on stg.gid = stgl.group_id
                    // left outer join sms_target_member_link stml 
                    // on stgl.group_id = stml.gid 
                    // left outer join sms_target_member stm 
                    // on stml.mid = stm.mid 
                    // where 1=1
                    // and stg.status = 1
                    // and stg.activation = 1
                    // and stgl.data_type = 'NQC'
                    // and stm.activation = 1
                    // and stgl.data_kind = 'RDR' -- param
                    // and stgl.site = 'KWK' -- param
                    // order by stm.name asc
                    // ;
                    // and  -- 장애
                    // ;

                    String call_to = "01011112222";
                    String call_from = DataCommon.getInfoConf("siteInfo", "call_from");
                    
                    //app sequence
                    Long appSeq = Long.parseLong(smsSendRepository.getAppContentNextval());

                    //문자 전송(app_send_data) insert
                    smsSendRepository.gaonAppSendDataSave(appSeq, call_to, call_from); //템플릿 코드 넣어야함.
                    //문자 전송(app_send_contents) insert
                    smsSendRepository.gaonAppSendContentsSave(appSeq, smsPettern);

                }

                // for(int b=0; b<rdDto.size(); b++){
                    
                // }
            }

        }
        
    }
    
}
