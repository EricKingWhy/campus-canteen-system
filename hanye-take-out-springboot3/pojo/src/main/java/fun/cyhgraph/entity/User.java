package fun.cyhgraph.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    private Integer id;
    private String name;
    private String openid;
    private String phone;
    private Integer gender;
    private Integer age;
    private Double activityFactor;
    private Double height;
    private Double weight;
    private String allergies;
    private String idNumber;
    private String pic;
    private LocalDateTime createTime;

}
