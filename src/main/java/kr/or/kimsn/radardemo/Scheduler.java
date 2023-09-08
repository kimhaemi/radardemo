package kr.or.kimsn.radardemo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kr.or.kimsn.radardemo.dto.repository.ReceiveSettingRepository;
import kr.or.kimsn.radardemo.dto.repository.StationRepository;
import kr.or.kimsn.radardemo.process.StepOneProcess;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class Scheduler {

    private final StationRepository stationRepository;
    private final ReceiveSettingRepository receiveSettingRepository;

    //     *           *　　　　　　*　　　　　　*　　　　　　*　　　　　　*
    // 초(0-59)   분(0-59)　　시간(0-23)　　일(1-31)　　월(1-12)　　요일(0-7) 
    //@Scheduled(cron = "30 0,5,1/0,15,20,25,30,35,40,45,50,55 * * * ?") // 5분 30초
    @Scheduled(fixedDelay = 20000) //20초마다
    @Async
    public void cronJobSch() throws InterruptedException {
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("[site 접속 및 파일 처리 Start] : " + LocalDateTime.now().format(dtf));
        System.out.println("[=================== 1번째 프로세스 ===================]");
        
        StepOneProcess mt = new StepOneProcess(stationRepository,receiveSettingRepository);
        mt.start();
        
        System.out.println("[site 접속 및 파일 처리 end] : " + LocalDateTime.now().format(dtf));
        
        Thread.sleep(60000); //60초
        System.out.println("[20초 후 다음] : " + LocalDateTime.now().format(dtf));
        System.out.println("[=================== 2번째 프로세스 ===================]");

    }


    
}
