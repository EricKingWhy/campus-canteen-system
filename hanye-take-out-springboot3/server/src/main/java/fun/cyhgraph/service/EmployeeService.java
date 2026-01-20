package fun.cyhgraph.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.cyhgraph.dto.EmployeeDTO;
import fun.cyhgraph.dto.EmployeeLoginDTO;
import fun.cyhgraph.dto.EmployeePageQueryDTO;
import fun.cyhgraph.entity.Employee;
import fun.cyhgraph.result.PageResult;

public interface EmployeeService extends IService<Employee> {

    Employee login(EmployeeLoginDTO employeeLoginDTO);

    void save(EmployeeDTO employeeDTO);

    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    // 【核心】改为 Integer
    void startOrStop(Integer status, Integer id);

    // 【核心】改为 Integer
    Employee getById(Integer id);

    void update(EmployeeDTO employeeDTO);
}
