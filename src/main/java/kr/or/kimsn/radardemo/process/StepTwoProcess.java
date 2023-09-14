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

        String currentTime = FormatDateUtil.formatDate("yyyy-MM-dd HH:mm:ss", new Date());
        
        int gubun = Integer.parseInt(DataCommon.getInfoConf("siteInfo", "gubun"));
        // System.out.println("[데몬 구분 int] : " + gubun);
        
        if (gubun == 1) gubunStr = "대형";
        if (gubun == 2) gubunStr = "소형";
        if (gubun == 3) gubunStr = "공항";
        System.out.println("[데몬 구분] : " + gubunStr);

        if (gubun == 1) dataKindStr = "RDR";
        if (gubun == 2) dataKindStr = "SDR";
        if (gubun == 3) dataKindStr = "TDWR";
        System.out.println("[데이터 종류] : " + dataKindStr);

        String mode = DataCommon.getInfoConf("siteInfo", "mode");
        // System.out.println("[mode] : " + mode);

        if (!mode.equals("test")) {
            srDto = queryService.getStation(gubun);
            srCnt = srDto.size();
        }

        System.out.println("[접속 레이더 갯수] : " + srCnt);

        int all_site_network_no = 0; //전 사이트 네트워크 장애 여부
        
        // 경고 기준 (횟수 - criterion)
        List<ReceiveConditionCriteriaDto> rccDto = queryService.getReceiveConditionCriteriaList(gubun);

        for(int a=0; a< srCnt; a++){
            String site_cd = srDto.get(a).getSiteCd();

            //전 site 이력 조회
            List<ReceiveDataDto> rdDtoSingle = queryService.getReceiveDataList(site_cd, dataKindStr, 1);
            for(ReceiveDataDto rd : rdDtoSingle){
                if(rd.getRecv_condition().equals("MISS") && rd.getCodedtl().equals("siteconnect_no")){
                    all_site_network_no++;
                }
            }
        }
        
        for(int a=0; a< srCnt; a++){

            String new_recv_condition = ""; //정상(ok) / 장애(file_no, filesize_no, siteconnect_no) / 네트워크 장애(network_no)
            String recv_con_dtl = ""; //정상(ok) / 장애(file_no, filesize_no, siteconnect_no) / 네트워크 장애(network_no)

            String site_cd = srDto.get(a).getSiteCd();
            String siteStr = srDto.get(a).getName_kr();

            System.out.println("[============ " + siteStr + " 정보 ==============]");

            if(all_site_network_no == srCnt){
                System.out.println("============================== 전 사이트 네트워크 장애 ================================");
                // 이력 update
                queryService.updateReceiveData("TOTA", "network_no", site_cd, dataKindStr, "NQC", "MISS", currentTime);
            }

            // 최종상태조회
            ReceiveConditionDto rcDto = queryService.getReceiveCondition(dataKindStr, "NQC", site_cd);
            //최종 상태가 TOTA 이면 네트워크 복구 이력으로 update
            if(rcDto.getRecv_condition().equals("TOTA")){
                // 이력 update
                queryService.updateReceiveData("TORE", "network_ok", site_cd, dataKindStr, "NQC", "RECV", currentTime);
            }
            //최종 상태가 WARN 에서 
            if(rcDto.getRecv_condition().equals("WARN")){
                // 이력 update
                //자료가 수신되었을 때
                if(rcDto.getCodedtl().equals("file_no")){
                    queryService.updateReceiveData("RETR", "file_ok", site_cd, dataKindStr, "NQC", "RECV", currentTime);    
                }

                //파일크기가 정상 수신되었을때
                if(rcDto.getCodedtl().equals("filesize_no")){
                    queryService.updateReceiveData("RETR", "filesize_ok", site_cd, dataKindStr, "NQC", "RECV", currentTime);
                }
            }

            for(ReceiveConditionCriteriaDto rcc : rccDto){
                
                //복구 > 정상
                if(rcDto.getRecv_condition().equals("RETR")){
                    //정상(ok)
                    List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(site_cd, dataKindStr, 1);
                    for(ReceiveDataDto rd : rdDto){
                        if(rd.getRecv_condition().equals("RECV") && rd.getCodedtl().equals("ok")){
                            new_recv_condition = "ORDI";
                            recv_con_dtl = "ok";
                        }
                    }
                }

                //장애 > 복구 - 최종 상태가 '주의 또는 경고' 상태에서 자료가 연속으로 N회 이상 자료가 수신되었을 때
                if(rcDto.getRecv_condition().equals("WARN")){
                    //이력조회
                    List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(site_cd, dataKindStr, rcc.getCriterion());
                    
                    //복구(file_ok)
                    if(rcc.getCodedtl().equals("file_ok")){
                        int cnt = 0;
                        for(ReceiveDataDto rd : rdDto){
                            if(rd.getCodedtl().equals(rcc.getCodedtl())){
                                cnt++;
                            }
                        }
                        //이력 비교
                        if(rcc.getCriterion() == cnt){
                            new_recv_condition = "RETR";
                            recv_con_dtl = rcc.getCodedtl();
                        }
                    }
                    //복구(filesize_ok)
                    if(rcc.getCodedtl().equals("filesize_ok")){
                        int cnt = 0;
                        for(ReceiveDataDto rd : rdDto){
                            if(rd.getCodedtl().equals(rcc.getCodedtl())){
                                cnt++;
                            }
                        }
                        //이력 비교
                        if(rcc.getCriterion() == cnt){
                            new_recv_condition = "RETR";
                            recv_con_dtl = rcc.getCodedtl();
                        }
                    }
                }

                //네트워크 복구(network_ok)
                if(rcc.getCode().equals("TORE")){
                    //이력조회
                    List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(site_cd, dataKindStr, rcc.getCriterion());
                    int cnt = 0;
                    for(ReceiveDataDto rd : rdDto){
                        if(rd.getRecv_condition().equals(rcc.getCode())){
                            cnt++;
                        }
                    }
                    if(rcc.getCriterion() == cnt){
                        System.out.println("네트워크 복구(network_ok)");
                        new_recv_condition = "TORE";
                        recv_con_dtl = "network_ok";
                    }

                } else

                //네트워크 장애(network_no)
                if(rcc.getCode().equals("TOTA")){
                    //이력조회
                    List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(site_cd, dataKindStr, rcc.getCriterion());
                    int cnt = 0;
                    for(ReceiveDataDto rd : rdDto){
                        if(rd.getRecv_condition().equals(rcc.getCodedtl())){
                            cnt++;
                        }
                    }
                    if(rcc.getCriterion() == cnt){
                        System.out.println("네트워크 장애(network_no)");
                        new_recv_condition = "TOTA";
                        recv_con_dtl = "network_no";
                    }
                } else 

                //장애
                if(rcc.getCode().equals("WARN")){
                    //이력조회
                    List<ReceiveDataDto> rdDto = queryService.getReceiveDataList(site_cd, dataKindStr, rcc.getCriterion());

                    //장애(file_no)
                    if(rcc.getCodedtl().equals("file_no")){
                        int cnt = 0;
                        for(ReceiveDataDto rd : rdDto){
                            if(rd.getCodedtl().equals(rcc.getCodedtl())){
                                cnt++;
                            }
                        }
                        //이력 비교
                        if(rcc.getCriterion() == cnt){
                            new_recv_condition = "WARN";
                            recv_con_dtl = "file_no";
                        }
                    }
                    //장애(filesize_no)
                    if(rcc.getCodedtl().equals("filesize_no")){
                        int cnt = 0;
                        for(ReceiveDataDto rd : rdDto){
                            if(rd.getCodedtl().equals(rcc.getCodedtl())){
                                cnt++;
                            }
                        }
                        //이력 비교
                        if(rcc.getCriterion() == cnt){
                            new_recv_condition = "WARN";
                            recv_con_dtl = "filesize_no";
                        }
                    }
                    //장애(siteconnect_no)
                    if(rcc.getCodedtl().equals("siteconnect_no")){
                        int cnt = 0;
                        for(ReceiveDataDto rd : rdDto){
                            if(rd.getCodedtl().equals(rcc.getCodedtl())){
                                cnt++;
                            }
                        }
                        //이력 비교
                        if(rcc.getCriterion() == cnt){
                            new_recv_condition = "WARN";
                            recv_con_dtl = "siteconnect_no";
                            // all_site_network_no++;
                        }
                    }
                }
            }
            System.out.println("[결과] : " + new_recv_condition);
            System.out.println("[결과 상세] : " + recv_con_dtl);

            if(!new_recv_condition.equals("")){
                // 최종상태조회
                // ReceiveConditionDto rcDto = queryService.getReceiveCondition(dataKindStr, "NQC", site_cd);
                // System.out.println("[최종상태 조회] : " + rcDto);
                int sms_send = 0;
                if(rcDto.getRecv_condition().equals(new_recv_condition)){
                    sms_send = rcDto.getSms_send();
                } else {
                    sms_send = 0;
                }

                // 최종상태 update
                System.out.println(siteStr + " 별 최종상태 update");
                queryService.insReceiveCondition(site_cd, dataKindStr, dataType, new_recv_condition, rcDto.getApply_time(), currentTime, sms_send, rcDto.getSms_send_activation(), 1, recv_con_dtl);
                // Integer total = queryService.updateReceiveCondition(new_recv_condition, 0, where_recv_condition, site_cd, dataKindStr, dataType);
            } else {
                System.out.println("[상태 안바뀜]");
            }
        }
    }
}
