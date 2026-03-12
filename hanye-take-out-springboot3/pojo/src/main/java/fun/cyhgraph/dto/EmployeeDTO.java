package fun.cyhgraph.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeDTO implements Serializable {
    private Long id;
    private String name;
    
    @JsonAlias({"username", "account"})
    private String account;
    
    private String password;
    private String phone;
    private Integer age;
    private Integer gender;
    private String pic;
}
