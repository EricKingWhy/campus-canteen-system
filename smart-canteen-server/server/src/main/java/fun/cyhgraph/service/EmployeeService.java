package fun.cyhgraph.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.cyhgraph.dto.EmployeeDTO;
import fun.cyhgraph.dto.EmployeeLoginDTO;
import fun.cyhgraph.dto.EmployeePageQueryDTO;
import fun.cyhgraph.dto.EmployeeRegisterDTO;
import fun.cyhgraph.entity.Employee;
import fun.cyhgraph.result.PageResult;

public interface EmployeeService extends IService<Employee> {

    Employee login(EmployeeLoginDTO employeeLoginDTO);

    void register(EmployeeRegisterDTO employeeRegisterDTO);

    void save(EmployeeDTO employeeDTO);

    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    // 【核心】升级为 Long
    void startOrStop(Integer status, Long id);

    // 【核心】升级为 Long
    Employee getById(Long id);

    void update(EmployeeDTO employeeDTO);

    void deleteById(Long id);
}
