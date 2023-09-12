package kr.or.kimsn.radardemo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import kr.or.kimsn.radardemo.dto.ReceiveConditionDto;
import kr.or.kimsn.radardemo.dto.ReceiveDataDto;
import kr.or.kimsn.radardemo.dto.ReceiveSettingDto;
import kr.or.kimsn.radardemo.dto.SmsSendOnOffDto;
import kr.or.kimsn.radardemo.dto.StationDto;
import kr.or.kimsn.radardemo.dto.repository.ReceiveConditionRepository;
import kr.or.kimsn.radardemo.dto.repository.ReceiveDataRepository;
import kr.or.kimsn.radardemo.dto.repository.ReceiveSettingRepository;
import kr.or.kimsn.radardemo.dto.repository.SmsSendOnOffRepository;
import kr.or.kimsn.radardemo.dto.repository.StationRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class QueryService {

    private final StationRepository stationRepository; //site
    private final ReceiveSettingRepository receiveSettingRepository;
    private final SmsSendOnOffRepository smsSendOnOffRepository;

    private final ReceiveConditionRepository receiveConditionRepository; //최종
    private final ReceiveDataRepository receiveDataRepository; //이력

    //site 조회
    public List<StationDto> getStation(int gubun){
        return stationRepository.findByGubunOrderBySortOrder(gubun);
    }

    //자료 수신 처리 설정 조회
    public ReceiveSettingDto getrRceiveSetting (String dataKindStr){
        return receiveSettingRepository.findByDataKindAndPermittedWatchAndStatus(dataKindStr, 1, 1);
    }
    
    public void InsReceiveCondition (String site_cd, String dataKindStr, String dataType, String recv_condition, 
                                     String apply_time, String last_check_time, int sms_send, int status, String codedtl) {
        ReceiveConditionDto rcDto = new ReceiveConditionDto();

        SmsSendOnOffDto onOffDto = smsSendOnOffRepository.findByCode("STOPSMS");
        int onOffVal = onOffDto.getValue();

        System.out.println(" site : " + site_cd);
        System.out.println(" data_kind : " + dataKindStr);
        System.out.println(" data_type : " + dataType);
        System.out.println(" recv_condition : " + recv_condition);
        System.out.println(" apply_time : " + apply_time);
        System.out.println(" last_check_time : " + last_check_time);
        System.out.println(" sms_send : " + sms_send);
        System.out.println(" sms_send_activation : " + onOffVal);
        System.out.println(" status : " + status);
        System.out.println(" codedtl : " + codedtl);
        
        rcDto.setSite(site_cd);
        rcDto.setDataKind(dataKindStr);
        rcDto.setDataType(dataType);
        rcDto.setRecv_condition(recv_condition);
        rcDto.setApply_time(apply_time);
        rcDto.setLast_check_time(last_check_time);
        rcDto.setSms_send(sms_send);
        rcDto.setSms_send_activation(onOffVal);
        rcDto.setStatus(status);
        rcDto.setCodedtl(codedtl);
        
        receiveConditionRepository.save(rcDto);
    }

    public void InsReceiveData (String dataKindStr, String site_cd, String dataType, String data_time, String data_kst, String recv_time,
                                String recv_condition, String recv_condition_check_time, String file_name, Long file_size, String codedtl) {
        ReceiveDataDto rdDto = new ReceiveDataDto();

        System.out.println("data_kind : " + dataKindStr);
        System.out.println("site : " + site_cd);
        System.out.println("data_type : " + dataType);
        System.out.println("data_time : " + data_time);
        System.out.println("data_kst : " + data_kst);
        System.out.println("recv_time : " + recv_time);
        System.out.println("recv_condition : " + recv_condition);
        System.out.println("recv_condition_check_time : " + recv_condition_check_time);
        System.out.println("file_name : " + file_name);
        System.out.println("file_size : " + file_size);
        System.out.println("codedtl : " + codedtl);

        rdDto.setData_kind(dataKindStr);
        rdDto.setSite(site_cd);
        rdDto.setData_type(dataType);
        rdDto.setData_time(data_time);
        rdDto.setData_kst(data_kst);
        rdDto.setRecv_time(null);
        rdDto.setRecv_condition(recv_condition);
        rdDto.setRecv_condition_check_time(recv_condition_check_time);
        rdDto.setFile_name(null);
        rdDto.setFile_size(0);
        rdDto.setCodedtl(codedtl);

        receiveDataRepository.save(rdDto);
    }

    
}
