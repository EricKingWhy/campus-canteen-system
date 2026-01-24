package fun.cyhgraph.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserRegisterDTO implements Serializable {
    private String username; // 用户名/学号
    private String password; // 密码
    private String email; // 邮箱（可选，用于找回密码）
    private String nickname; // 昵称（前端注册时填写，映射到 User.name）
}
