package fun.cyhgraph.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;

    // 【核心】ID 统一为 Integer
    private Integer id;

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
