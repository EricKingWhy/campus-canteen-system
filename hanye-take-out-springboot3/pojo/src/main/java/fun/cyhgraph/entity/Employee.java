package fun.cyhgraph.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;

    // 【核心】ID 升级为 Long，防止 Snowflake ID 溢出
    private Long id;

    private String username;
    private String name;
    private String password;
    private String phone;
    private String sex;
    private String idNumber; // 身份证号
    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
