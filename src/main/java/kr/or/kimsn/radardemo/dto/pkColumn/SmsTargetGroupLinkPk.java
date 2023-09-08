package kr.or.kimsn.radardemo.dto.pkColumn;

import java.io.Serializable;

import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsTargetGroupLinkPk implements Serializable{
    private String site;
    private String data_kind;
    private String data_type;
    private String group_id;
}
