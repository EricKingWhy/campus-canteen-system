package fun.cyhgraph.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeRegisterDTO implements Serializable {
    @JsonAlias({"account", "username"})
    private String account;

    private String password;

    private String repassword;
}
