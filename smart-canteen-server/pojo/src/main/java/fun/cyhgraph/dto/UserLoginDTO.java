package fun.cyhgraph.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserLoginDTO implements Serializable {
    private String username; // 用户名/学号
    private String password; // 密码
    private String code; // 微信登录 code
}
