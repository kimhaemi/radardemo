package kr.or.kimsn.radardemo.common;

import java.io.FileInputStream;
import java.util.Properties;

public class DataCommon {

    private final static String siteInfo = "./conf/siteInfoSetting.conf";
    private final static String ipInfo = "./../ipConf/siteIPInfoSetting.conf";

    public static String getInfoConf(String infoStr, String tp){
        String confInfo = "";
        FileInputStream ipInfoIn = null;

        try {
            Properties ipInfoProps = new Properties();
            if(infoStr.equals("siteInfo")) ipInfoIn = new FileInputStream(siteInfo);
            if(infoStr.equals("ipInfo")) ipInfoIn = new FileInputStream(ipInfo);
            
            ipInfoProps.load(ipInfoIn);
            confInfo = ipInfoProps.getProperty(tp);
            
        } catch (Exception e) {
            System.out.println("사이트 Info 환경설정 정보 읽어오기 실패!!");
        }
        return confInfo;
    }
    
}
