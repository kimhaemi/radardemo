package kr.or.kimsn.radardemo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kr.or.kimsn.radardemo.common.DataCommon;
import kr.or.kimsn.radardemo.process.StepOneProcess;
import kr.or.kimsn.radardemo.process.StepThreeProcess;
import kr.or.kimsn.radardemo.process.StepTwoProcess;
import kr.or.kimsn.radardemo.service.QueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class Scheduler {

    private final QueryService queryService;

    // private String crontabSeconds = DataCommon.getInfoConf("ipInfo", "crontabSeconds");
    // private String crontabHours = DataCommon.getInfoConf("ipInfo", "crontabHours");
    // private String crontabDays = DataCommon.getInfoConf("ipInfo", "crontabDays");
    // private String crontabMonth = DataCommon.getInfoConf("ipInfo", "crontabMonth");
    // private String crontabWeek = DataCommon.getInfoConf("ipInfo", "crontabWeek");

    //     *           *　　　　　　*　　　　　　*　　　　　　*　　　　　　*
    // 초(0-59)   분(0-59)　　시간(0-23)　　일(1-31)　　월(1-12)　　요일(0-7)
    // @Scheduled(cron = "0 1,6,11,16,21,26,31,36,41,46,51,56 * * * ?") // 6분 00초 //대형, 공항
    // @Scheduled(cron = "30 0-59 * * * ?") // 매분 30초 //소형
    @Scheduled(fixedDelay = 30000) //30초마다
    @Async
    public void cronJobSch() throws InterruptedException {
        int PauseTime = Integer.parseInt(DataCommon.getInfoConf("ipInfo", "PauseTime"));
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("[=================== 1번째 프로세스 ===================] " + LocalDateTime.now().format(dtf));
        // System.out.println("[site 접속 및 파일 처리 Start] : " + LocalDateTime.now().format(dtf));
        
        StepOneProcess oneProc = new StepOneProcess(queryService);
        oneProc.start();
        
        // Thread.sleep(PauseTime*1000); //20초
        System.out.println("["+PauseTime+"초 후 다음] : " + LocalDateTime.now().format(dtf));
        System.out.println("[=================== 2번째 프로세스 ===================] " + LocalDateTime.now().format(dtf));
        // System.out.println("[결과 data update - 복구, 정상 등등]");
        
        // StepTwoProcess twoProc = new StepTwoProcess(queryService);
        // twoProc.stepTwo();

        // Thread.sleep(PauseTime*1000); //20초
        System.out.println("["+PauseTime+"초 후 다음] : " + LocalDateTime.now().format(dtf));
        System.out.println("[=================== 3번째 프로세스 ===================] " + LocalDateTime.now().format(dtf));
        // System.out.println("[문자 전송 여부 체크 start] : " + LocalDateTime.now().format(dtf));

        // StepThreeProcess threeProc = new StepThreeProcess(queryService);
        // threeProc.stepThree();
        
        // System.out.println("[문자 전송 여부 체크 end] : " + LocalDateTime.now().format(dtf));


    }


    
}
