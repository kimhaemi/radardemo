package kr.or.kimsn.radardemo.dto;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class SmsTargetGroupLinkListDto {
    @Id
    private String site;
    private String data_kind;
    private String data_type;
    private String group_id;

    private String name_kr;
    private String data_name;
}
