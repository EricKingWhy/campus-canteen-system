package fun.cyhgraph.controller.admin;

import fun.cyhgraph.constant.JwtClaimsConstant;
import fun.cyhgraph.dto.EmployeeDTO;
import fun.cyhgraph.dto.EmployeeLoginDTO;
import fun.cyhgraph.dto.EmployeePageQueryDTO;
import fun.cyhgraph.dto.EmployeeRegisterDTO;
import fun.cyhgraph.entity.Employee;
import fun.cyhgraph.properties.JwtProperties;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.result.Result;
import fun.cyhgraph.service.EmployeeService;
import fun.cyhgraph.utils.JwtUtil;
import fun.cyhgraph.vo.EmployeeLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);
        Employee employee = employeeService.login(employeeLoginDTO);

        // 生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());

        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        // 构建返回对象
        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername()) // 【匹配】这里对应 VO 中的 userName 字段
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    @PostMapping("/register")
    public Result register(@RequestBody EmployeeRegisterDTO employeeRegisterDTO) {
        log.info("管理员注册：{}", employeeRegisterDTO.getAccount());
        employeeService.register(employeeRegisterDTO);
        return Result.success();
    }

    @PostMapping
    public Result save(@RequestBody EmployeeDTO employeeDTO) {
        log.info("新增员工：{}", employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("员工分页查询，参数：{}", employeePageQueryDTO);
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("启用禁用员工账号：{}, {}", status, id);
        employeeService.startOrStop(status, id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

    @PutMapping
    public Result update(@RequestBody EmployeeDTO employeeDTO) {
        log.info("编辑员工信息：{}", employeeDTO);
        employeeService.update(employeeDTO);
        return Result.success();
    }

    @DeleteMapping
    public Result delete(@RequestParam Long id) {
        log.info("删除员工：{}", id);
        employeeService.deleteById(id);
        return Result.success();
    }
}
