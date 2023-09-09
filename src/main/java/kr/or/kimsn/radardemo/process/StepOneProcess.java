package kr.or.kimsn.radardemo.process;

import java.util.List;

import org.springframework.stereotype.Service;

import kr.or.kimsn.radardemo.common.DataCommon;
import kr.or.kimsn.radardemo.common.SftpUtil;
import kr.or.kimsn.radardemo.dto.StationDto;
import kr.or.kimsn.radardemo.dto.repository.ReceiveConditionRepository;
import kr.or.kimsn.radardemo.dto.repository.ReceiveDataRepository;
import kr.or.kimsn.radardemo.dto.repository.ReceiveSettingRepository;
import kr.or.kimsn.radardemo.dto.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class StepOneProcess extends Thread {

    private final StationRepository stationRepository;
    private final ReceiveSettingRepository receiveSettingRepository;

    private ReceiveConditionRepository receiveConditionRepository; //최종
    private ReceiveDataRepository receiveDataRepository; //이력
    
    @Override
    public void run() {
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
        
        System.out.println("[radar size] : " + srCnt);

        for(int a=0; a< srCnt; a++){
            String site_cd = "";
            if(srCnt == 1) site_cd = "TEST";
            if(srCnt > 1) site_cd = srDto.get(a).getSiteCd();

            int port = Integer.parseInt(DataCommon.getInfoConf("ipInfo", "PORT"));
            String site_ip = DataCommon.getInfoConf("ipInfo", site_cd+"_IP");
            String site_username = DataCommon.getInfoConf("ipInfo", site_cd+"_ID");
            String site_pwd = DataCommon.getInfoConf("ipInfo", site_cd+"_PASSWORD");

            try {

                System.out.println("[" + site_cd+" site 접속 정보]");
                System.out.println("[" + site_cd+"_port] : "+port);
                System.out.println("[" + site_cd+"_ip] : "+site_ip);
                System.out.println("[" + site_cd+"_username] : "+site_username);
                System.out.println("[" + site_cd+"_pwd] : "+site_pwd);
                
                //site 접속
                SftpUtil sftp = new SftpUtil(receiveSettingRepository);
                boolean sftpConnect = sftp.open(site_ip, site_username, site_pwd, port);

                System.out.println("[접속 유무] : " + sftpConnect);
                
                //접속 O
                if(sftpConnect){
                    String dataKindStr = "";

                     String file_path = DataCommon.getInfoConf("siteInfo", "rdr_path");

                    if(gubun == 1) dataKindStr = "RDR";
                    if(gubun == 2) dataKindStr = "SDR";
                    if(gubun == 3) dataKindStr = "TDWR";

                    boolean file_exists = sftp.fileExists(file_path, site_cd, dataKindStr);
                    System.out.println("[파일존재유무] : " + file_exists);
                    
                    //파일 O (ORDI - file_ok)
                    if(file_exists){
                        Long file_size_min = Long.parseLong(DataCommon.getInfoConf("siteInfo", "file_size_min"));
                        Long file_size_max = Long.parseLong(DataCommon.getInfoConf("siteInfo", "file_size_max"));

                        boolean fileSize = sftp.fileSize(file_path, file_size_min, file_size_max);
                        
                        //파일 품질 정상 (ORDI - filesize_ok)
                        if(fileSize){
                            System.out.println("[자료 수신 (ORDI - filesize_ok) query insert table1 - receive_condition]");
                            System.out.println("[자료 수신 (ORDI - filesize_ok) query insert table2 - receive_data]");
                        } else {
                        //파일 품질 이상 (WARN - filesize_no)
                            System.out.println("[파일 품질 이상 (WARN - filesize_no) query insert table1 - receive_condition]");
                            System.out.println("[파일 품질 이상 (WARN - filesize_no) query insert table2 - receive_data]");
                        }
                    
                    } else {
                    //파일 X (WARN - file_no)
                        System.out.println("[자료 미수신 (WARN - filesize_no) query insert table1 - receive_condition]");
                        System.out.println("[자료 미수신 (WARN - filesize_no) query insert table2 - receive_data]");
                    }
                } else {
                //접속 X (TOTA)
                    System.out.println("[접속 실패 query insert table1 - receive_condition]");
                    System.out.println("[접속 실패 query insert table2 - receive_data]");
                }
            } catch (Exception e) {
                // log.debug("error : " + e);
                System.out.println("StepOneProcess run Error - " + e);
            }
        }
    }
}
