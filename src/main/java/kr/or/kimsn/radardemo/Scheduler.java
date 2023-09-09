package kr.or.kimsn.radardemo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kr.or.kimsn.radardemo.dto.repository.ReceiveConditionCriteriaRepository;
import kr.or.kimsn.radardemo.dto.repository.ReceiveConditionRepository;
import kr.or.kimsn.radardemo.dto.repository.ReceiveDataRepository;
import kr.or.kimsn.radardemo.dto.repository.ReceiveSettingRepository;
import kr.or.kimsn.radardemo.dto.repository.SmsSendPatternRepository;
import kr.or.kimsn.radardemo.dto.repository.SmsSendRepository;
import kr.or.kimsn.radardemo.dto.repository.StationRepository;
import kr.or.kimsn.radardemo.process.StepOneProcess;
import kr.or.kimsn.radardemo.process.StepTwoProcess;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class Scheduler {

    private final StationRepository stationRepository;
    private final ReceiveConditionRepository receiveConditionRepository;
    private final ReceiveSettingRepository receiveSettingRepository;
    private final ReceiveDataRepository receiveDataRepository;
    private final ReceiveConditionCriteriaRepository receiveConditionCriteriaRepository;

    private final SmsSendPatternRepository smsSendPatternRepository;//문자메시지 패턴
    private final SmsSendRepository smsSendRepository; //문자 메시지 전송(app_send_data, app_send_contents)

    //     *           *　　　　　　*　　　　　　*　　　　　　*　　　　　　*
    // 초(0-59)   분(0-59)　　시간(0-23)　　일(1-31)　　월(1-12)　　요일(0-7) 
    //@Scheduled(cron = "30 0,5,1/0,15,20,25,30,35,40,45,50,55 * * * ?") // 5분 30초
    @Scheduled(fixedDelay = 30000) //20초마다
    @Async
    public void cronJobSch() throws InterruptedException {
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("[site 접속 및 파일 처리 Start] : " + LocalDateTime.now().format(dtf));
        System.out.println("[=================== 1번째 프로세스 ===================]");
        
        StepOneProcess oneProc = new StepOneProcess(stationRepository,receiveSettingRepository);
        oneProc.start();
        
        System.out.println("[site 접속 및 파일 처리 end] : " + LocalDateTime.now().format(dtf));
        
        Thread.sleep(5000); //60초
        System.out.println("[20초 후 다음] : " + LocalDateTime.now().format(dtf));
        System.out.println("[=================== 2번째 프로세스 ===================]");
        System.out.println("[문자 전송 여부 체크 start] : " + LocalDateTime.now().format(dtf));
        StepTwoProcess twoProc = new StepTwoProcess(stationRepository, receiveConditionRepository, receiveDataRepository, receiveConditionCriteriaRepository, smsSendPatternRepository, smsSendRepository);
        twoProc.stepTwo();
        System.out.println("[문자 전송 여부 체크 end] : " + LocalDateTime.now().format(dtf));


    }


    
}
