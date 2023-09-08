package kr.or.kimsn.radardemo.dto;

import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Data;

@Data
@Entity
public class SmsTargetGroupMemberDto {

    @Id
    private Long mid;
    private Long gid;
    private String name;
    private String organization;

    private String department;
    private String position;
    private String phone_num;

    private Integer noti;
    private Integer warn;
    private Integer tota;
    private Integer retr;
    private Integer sms;

}
