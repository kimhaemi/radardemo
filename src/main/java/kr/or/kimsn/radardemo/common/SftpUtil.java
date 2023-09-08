package kr.or.kimsn.radardemo.common;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

import org.h2.mvstore.DataUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataUnit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import kr.or.kimsn.radardemo.dto.ReceiveSettingDto;
import kr.or.kimsn.radardemo.dto.repository.ReceiveSettingRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class SftpUtil {

    private JSch jSch;
    private Channel channel;
    private ChannelSftp channelSftp;

    private final ReceiveSettingRepository receiveSettingRepository;

    //sftp 서버 연결
    public boolean open(String host, String id, String password, int port) { 
        boolean result = false;

        //JSch 객체를 생성
        jSch = new JSch();

        
        try {
            //privateKey 인증
            // jSch.addIdentity(privateKey);

            //JSchSession 객체를 생성 (사용자 이름, 접속할 호스트, 포트 전달)
            Session session = jSch.getSession(id, host, port);
            session.setPassword(password);

            //기타 설정 적용
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            //접속
            session.connect();

            //sftp 채널 열기 및 접속
            channel = session.openChannel("sftp");
            channel.connect(5000); //5초
            result = true;
            
            // 채널을 FTP 용 채널 객체로 캐스팅
            channelSftp = (ChannelSftp) channel;


        } catch (JSchException e) {
            result = false;
            e.printStackTrace();
            System.out.println("SFTP: server connect failed.");
            // log.error("SFTP: server connect failed.");
        }


        return result;
    }
    
    //sftp 서버 연결 종료
    private void close() { 
        
    }

    /**
     * 디렉토리( or 파일) 존재 여부
     * @param path 디렉토리 (or 파일)
     * @return
     */
    public boolean fileExists(String path, String siteCd, String dataKind) {
        System.out.println("[파일 경로] : " + path);
        

        //1. 자료감시설정 on
        //2. 파일 패턴으로 파일명 찾기
        try {
            ReceiveSettingDto rsDto = receiveSettingRepository.findByDataKindAndPermittedWatchAndStatus(dataKind, 1, 1);
            System.out.println(rsDto);
            String filePattern = rsDto.getFilename_pattern();
            String timeZone = rsDto.getTime_zone();
            String dateTime = "";
            
            if(timeZone.equals("K")) dateTime = FormatDateUtil.formatDate("yyyyMMddHHmm", new Date());
            if(timeZone.equals("U")) dateTime = FormatDateUtil.formatDate("yyyyMMddHHmm", new Date());
            System.out.println(FormatDateUtil.changeKstToUtc(new Date()));
        
            System.out.println("[date Time] : " + dateTime);

            String fileName = filePattern.replace("%SITE%", siteCd).replace("%yyyyMMddHHmm%", dateTime);
            System.out.println("[파일 패턴] : " + filePattern);
            System.out.println("[파일 명] : " + fileName);
        } catch (Exception e) {
            System.out.println("파일 존재여부 확인 에러 - " + e);
        }

        

        Vector res = null;
        try {
            res = channelSftp.ls(path);
            System.out.println("[파일 존재여부 확인]" + res.size());
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return false;
            }
        }
        return res != null && !res.isEmpty();
    }

    //파일 사이즈
    public boolean fileSize(String path, Long file_size_min, Long file_size_max) {
        System.out.println("[파일 경로] : " + path);
        // System.out.println("[파일 명] : " + file.getName());
        
        Vector res = null;
        try {
            res = channelSftp.ls(path);
            System.out.println("[파일 존재여부 확인]" + res.size());
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return false;
            }
        }
        return res != null && !res.isEmpty();
    }
    
    
}
