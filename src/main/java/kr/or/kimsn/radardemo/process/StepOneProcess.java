package kr.or.kimsn.radardemo.process;

import java.util.Date;
import java.util.List;

import kr.or.kimsn.radardemo.common.DataCommon;
import kr.or.kimsn.radardemo.common.FormatDateUtil;
import kr.or.kimsn.radardemo.common.SftpUtil;
import kr.or.kimsn.radardemo.dto.ReceiveSettingDto;
import kr.or.kimsn.radardemo.dto.StationDto;
import kr.or.kimsn.radardemo.service.QueryService;
import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// @Slf4j
@RequiredArgsConstructor
public class StepOneProcess extends Thread {

    private final QueryService queryService;

    private List<StationDto> srDto;

    @Override
    public void run() {
        String gubunStr = ""; // 데몬 구분
        String dataKindStr = ""; // 데이터 종류
        String dataType = "NQC"; // 데이터 타입
        int srCnt = 0; // for문 site 갯수

        String currentTime = FormatDateUtil.formatDate("yyyy-MM-dd HH:mm:ss", new Date());
        String data_time = FormatDateUtil.formatDate("yyyy-MM-dd HH:mm:ss", FormatDateUtil.changeKstToUtc(new Date()));
        String data_kst = currentTime;
        String recv_condition_check_time = currentTime;
        String recv_condition_data = "";
        String codedtl = "";
        String file_name = "";
        Long file_size = 0L;
        String errStrData = "";

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

        if (mode.equals("test"))
            srCnt = 1;
        if (!mode.equals("test")) {
            srDto = queryService.getStation(gubun);
            srCnt = srDto.size();
        }

        System.out.println("[접속 레이더 갯수] : " + srCnt);

        for (int a = 0; a < srCnt; a++) {
            String site_cd = "";
            if (srCnt == 1) site_cd = "TEST";
            if (srCnt > 1) site_cd = srDto.get(a).getSiteCd();

            int port = Integer.parseInt(DataCommon.getInfoConf("ipInfo", "PORT"));
            String site_ip = DataCommon.getInfoConf("ipInfo", site_cd + "_IP");
            String site_username = DataCommon.getInfoConf("ipInfo", site_cd + "_ID");
            String site_pwd = DataCommon.getInfoConf("ipInfo", site_cd + "_PASSWORD");
            if(srCnt > 1 && gubun == 2){
                site_pwd = DataCommon.getInfoConf("ipInfo", site_cd + "_PASSWORD")+"#";
            }

            try {

                System.out.println("[============ " + site_cd + " site 접속 정보 ==============]");
                System.out.println("[" + site_cd + "_port] : " + port);
                System.out.println("[" + site_cd + "_ip] : " + site_ip);
                System.out.println("[" + site_cd + "_username] : " + site_username);
                System.out.println("[" + site_cd + "_pwd] : " + site_pwd);

                // site 접속
                SftpUtil sftp = new SftpUtil();
                boolean sftpConnect = sftp.open(site_ip, site_username, site_pwd, port);

                System.out.println("[접속 유무] : " + sftpConnect);

                // 접속 O
                if (sftpConnect) {
                    String file_path = DataCommon.getInfoConf("siteInfo", "rdr_path");
                    if(gubun == 2){ //소형폴더는 조합이 다르네..
                        String yearmonth = FormatDateUtil.formatDate("yyyyMM", new Date()); //연월
                        String day = FormatDateUtil.formatDate("dd", new Date()); //일
                        file_path = file_path.replace("%yyyyMM%", yearmonth).replace("%dd%", day);
                    }
                    System.out.println("[file_path] : " + file_path);

                    ReceiveSettingDto rsDto = queryService.getrRceiveSetting(dataKindStr);
                    
                    String filePattern = rsDto.getFilename_pattern();
                    String timeZone = rsDto.getTime_zone();

                    String dateTime = FormatDateUtil.formatDate("yyyyMMddHHmm", new Date());

                    if(gubun != 2){
                        if (Integer.parseInt(dateTime.substring(dateTime.length() - 1, dateTime.length())) < 5)
                            dateTime = dateTime.substring(0, dateTime.length() - 1) + "0";
                        if (Integer.parseInt(dateTime.substring(dateTime.length() - 1, dateTime.length())) > 5)
                            dateTime = dateTime.substring(0, dateTime.length() - 1) + "5";
                    }
                    // System.out.println("[date Time] : " + dateTime);
                    // System.out.println("[site_cd] : " + site_cd);

                    // 파일 패턴으로 파일명 찾기
                    file_name = filePattern.replace("%site%", site_cd).replace("%yyyyMMddHHmm%", dateTime);
                    System.out.println("[파일 패턴] : " + filePattern);
                    System.out.println("[파일 명] : " + file_name);

                    // 자료감시 설정 on
                    if (rsDto.getPermittedWatch() == 1) {
                        // System.out.println("[자료감시 설정 on]");
                        boolean file_exists = sftp.fileExists(file_path, file_name, site_cd, dataKindStr, filePattern, timeZone);
                        System.out.println("[파일존재유무] : " + file_exists);

                        // 파일 O (ORDI - file_ok)
                        if (file_exists) {
                            // System.out.println("[파일 O]");
                            Long file_size_min = Long.parseLong(DataCommon.getInfoConf("siteInfo", "file_size_min"));
                            Long file_size_max = Long.parseLong(DataCommon.getInfoConf("siteInfo", "file_size_max"));

                            file_size = sftp.fileSize(file_path, file_name, file_size_min, file_size_max);
                            System.out.println("[file size] : " + file_size);

                            Long kb = file_size / 1024;
                            System.out.println("[kb] : " + kb);

                            // 파일 품질 정상 (ORDI - filesize_ok)
                            if (kb > file_size_min) {
                                codedtl = "ok";
                                // recv_condition = "ORDI";
                                recv_condition_data = "RECV";
                                // errStr = "[자료 수신 (ORDI - filesize_ok) query insert table1 - receive_condition]";
                                errStrData = "[자료 수신 (ORDI - filesize_ok) query insert receive_data]";
                                
                            } else {
                                // 파일 품질 이상 (WARN - filesize_no)
                                // recv_condition = "WARN";
                                codedtl = "filesize_no";
                                recv_condition_data = "MISS";
                                // errStr = "[파일 품질 이상 (WARN - filesize_no) query insert table1 - receive_condition]";
                                errStrData = "[파일 품질 이상 (WARN - filesize_no) query insert - receive_data]";
                            }

                        } else {
                            // 파일 X (WARN - file_no)
                            // recv_condition = "WARN";
                            codedtl = "file_no";
                            recv_condition_data = "MISS";
                            // errStr = "[자료 미수신 (WARN - file_no) query insert table1 - receive_condition]";
                            errStrData = "[자료 미수신 (WARN - file_no) query insert - receive_data]";
                        }

                    } else {
                        // 자료감시 설정 off
                        System.out.println("[자료감시 설정 off]");
                    }

                } else {
                    // 접속 X (WARN - MISS)
                    // recv_condition = "WARN";
                    codedtl = "siteconnect_no";
                    recv_condition_data = "MISS";

                    // errStr = "[접속 실패 query insert table1 - receive_condition]";
                    errStrData = "[접속 실패 query insert - receive_data]";
                }

                System.out.println("[파일 명] : " + file_name);
                System.out.println("[파일 size] : " + file_size);
                System.out.println("[파일 recv] : " + recv_condition_data);
                System.out.println("[파일 recvDtl] : " + codedtl);
                // System.out.println(errStr);
                System.out.println(errStrData);

                // queryService.insReceiveCondition(site_cd, dataKindStr, dataType, recv_condition, apply_time, last_check_time, sms_send, status, codedtl);
                queryService.insReceiveData(dataKindStr, site_cd, dataType, data_time, data_kst, data_kst, recv_condition_data, recv_condition_check_time, file_name, file_size, codedtl);
            } catch (Exception e) {
                // log.debug("error : " + e);
                System.out.println("StepOneProcess run Error - " + e);
            }
        }
    }
}
