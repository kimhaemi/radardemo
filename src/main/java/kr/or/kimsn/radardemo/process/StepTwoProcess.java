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
import kr.or.kimsn.radardemo.service.QueryService;
import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

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
        int srCnt = 0;

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
            String siteStr = "";
            if(srCnt == 1) {
                site_cd = "TEST";
                siteStr = "TEST";
            }
            if(srCnt > 1) { 
                site_cd = srDto.get(a).getSiteCd();
                siteStr = srDto.get(a).getName_kr(); 
            }

            // 최종상태조회
            ReceiveConditionDto rcDto = queryService.getReceiveCondition(dataKindStr, "NQC", site_cd);
            // System.out.println("[최종상태 조회] : " + rcDto);

            String code = rcDto.getRecv_condition();
            String codedtl = rcDto.getCodedtl();

            System.out.println("[gubun] : " + gubun);
            System.out.println("[code] : " + code);
            System.out.println("[codedtl] : " + codedtl);

            // 경고 기준 (횟수 - criterion)
            ReceiveConditionCriteriaDto rccDto = queryService.getReceiveConditionCriteria(gubun, code, codedtl);
            // System.out.println("[경고 기준] : " + rccDto);
            int criterion = rccDto.getCriterion();
            System.out.println("[site 별 '"+criterion+"'회 체크]");

            //문자메시지 패턴
            SmsSendPatternDto smsSendPatternDto = queryService.getSmsSendPattern(1, 1, "RUN", code, codedtl);
            // System.out.println("[문자메시지 패턴] : " + smsSendPatternDto);

            String dateTime = FormatDateUtil.formatDate("yyyy-MM-dd HH:mm:ss", new Date());
            
            // System.out.println("[date Time] : " + dateTime);

            String smsPettern = smsSendPatternDto.getPattern();
            smsPettern = smsPettern.replace("%SITE%", siteStr).replace("%TIME%", dateTime);

            System.out.println("[문자메시지 패턴] : " + smsPettern);
            
            //이력 조회
            List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(site_cd, dataKindStr, criterion);
            // System.out.println("[이력 조회] : " + rdDto);
            int cnt = 0; // 최종이력상태와 결과 값 다른것
            for(ReceiveDataDto dto : rdDto){
                String recv_con = "";
                // ORDI	정상	0	정상	1	ok
                // RETR	복구	3	자료 크기가 연속으로 N회 이상 정상일때	1	filesize_ok
                // RETR	복구	3	비 정상에서 복구되어 정상의 범위에 들 경우	1	file_ok
                // TORE	네트워크 복구	2	네트워크 장애 상태에서 복구되어 정상의 범위에 들때	1	network_ok
                // TOTA	네트워크 장애	3	대형 레이더 전 사이트 자료 미수신 일 때	1	network_no
                // WARN	경고	7	자료 크기가 기준파일크기보다 연속으로 N회 이상 작을때	1	filesize_no
                // WARN	경고	6	자료가 연속으로 N회 이상  자료 미수신일 때	1	file_no
                if(dto.getRecv_condition().equals("RECV")) recv_con = "ORDI"; // 정상
                if(dto.getRecv_condition().equals("MISS")) recv_con = "WARN"; // 경고
                if(dto.getRecv_condition().equals("RETR")) recv_con = "RETR"; // 복구 - 네트워크 복구
                if(dto.getRecv_condition().equals("TOTA")) recv_con = "TOTA"; // 네트워크장애
                
                if(recv_con.equals(code)){
                    cnt++;
                }
                // if(!dto.getCodedtl().equals(rcDto.getCodedtl())){
                //     cnt++;
                // }
            }
            System.out.println("[cnt] : " + cnt);

            if(rccDto.getCriterion() == cnt){
                System.out.println("문자 전송 해야함");
                //문자 전송(app_send_contents) insert
                // queryService.intGaonAppSendContentsSave(appSeq, smsPettern);
                

                //site 수신그룹 담당자에게 문자 전송
                // select 
                //   DISTINCT stm.name, 
                //   stm.phone_num,
                //   stml.warn, -- 경고 -- MISS
                //   stml.tota, -- 네트워크 오류
                //   stml.retr, -- 복구 -- 장애 복구, 네트워크 복구
                //   stml.sms  -- 문자 발송 여부
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
                
                // for(smsDto dto : siteDto){
                    String call_to = "01011112222";
                    String call_from = DataCommon.getInfoConf("siteInfo", "call_from");
                    
                    //app sequence
                    // Long appSeq = queryService.getAppContentNextval();

                    //문자 전송(app_send_data) insert
                    // queryService.insGaonAppSendDataSave(appSeq, call_to, call_from); //템플릿 코드 넣어야함.
                // }

                System.out.println("[문자 전송 여부 update]");
                int sms_send = 1;
                queryService.updateReceiveCondition(sms_send, code, site_cd, dataKindStr, "NQC");
            } else {
                System.out.println("[문자 전송 체크 - 기준자료] : " + rccDto.getCriterion());
                System.out.println("[문자 전송 체크 - data cnt] : " + cnt);
                System.out.println("문자 전송 안해도 됨");
            }

            // for(int b=0; b<rdDto.size(); b++){
                
            // }
        }
    }
    
}
