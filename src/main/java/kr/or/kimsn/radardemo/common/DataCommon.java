package kr.or.kimsn.radardemo.common;

import java.io.FileInputStream;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataCommon {

    public static String getInfoConf(String infoConf, String tp){
        String confInfo = "";

        try {
            Properties ipInfoProps = new Properties();
            FileInputStream ipInfoIn = new FileInputStream(infoConf);
            
            ipInfoProps.load(ipInfoIn);
            confInfo = ipInfoProps.getProperty(tp);
            
        } catch (Exception e) {
            System.out.println("사이트 Info 환경설정 정보 읽어오기 실패!!");
        }
        return confInfo;
    }
    
}
