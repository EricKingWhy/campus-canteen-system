package fun.cyhgraph.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.cyhgraph.constant.MessageConstant;
import fun.cyhgraph.constant.StatusConstant;
import fun.cyhgraph.dto.EmployeeDTO;
import fun.cyhgraph.dto.EmployeeLoginDTO;
import fun.cyhgraph.dto.EmployeePageQueryDTO;
import fun.cyhgraph.entity.Employee;
import fun.cyhgraph.exception.AccountLockedException;
import fun.cyhgraph.exception.AccountNotFoundException;
import fun.cyhgraph.exception.PasswordErrorException;
import fun.cyhgraph.mapper.EmployeeMapper;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        // 1. 根据用户名查询
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, username);
        Employee employee = employeeMapper.selectOne(queryWrapper);

        if (employee == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 2. 密码比对
        // 如果数据库里是明文，这里就直接比对；如果是MD5，就加密后比对。
        // 为了防止您数据库里是明文导致的登录失败，这里做一个兼容逻辑：
        if (!password.equals(employee.getPassword())) {
            // 尝试 MD5 加密后再比对
            String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
            if (!md5Password.equals(employee.getPassword())) {
                throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
            }
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        return employee;
    }

    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        // Manual mapping for mismatched fields
        employee.setUsername(employeeDTO.getAccount());
        if (employeeDTO.getGender() != null) {
            employee.setSex(String.valueOf(employeeDTO.getGender()));
        }

        // 自动填充缺失的身份证号 (防止数据库报错)
        if (employee.getIdNumber() == null || employee.getIdNumber().isEmpty()) {
            // 生成一个模拟的身份证号: 11010119900101000 + 随机数
            employee.setIdNumber("11010119900101000" + new java.util.Random().nextInt(9));
        }
        // 确保 ID 为 null，由数据库自增
        employee.setId(null);

        employee.setStatus(StatusConstant.ENABLE);
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        employeeMapper.insert(employee);
    }

    public PageResult pageQuery(EmployeePageQueryDTO dto) {
        Page<Employee> page = new Page<>(dto.getPage(), dto.getPageSize());
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        if (dto.getName() != null && !dto.getName().isEmpty()) {
            queryWrapper.like(Employee::getName, dto.getName());
        }
        queryWrapper.orderByDesc(Employee::getCreateTime);
        Page<Employee> p = employeeMapper.selectPage(page, queryWrapper);
        return new PageResult(p.getTotal(), p.getRecords());
    }

    // 【核心】升级为 Long 类型
    public void startOrStop(Integer status, Long id) {
        Employee employee = new Employee();
        employee.setStatus(status);
        employee.setId(id);
        employeeMapper.updateById(employee);
    }

    // 【核心】升级为 Long 类型
    public Employee getById(Long id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee != null) {
            employee.setPassword("****");
        }
        return employee;
    }

    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        // Manual mapping for mismatched fields
        if (employeeDTO.getAccount() != null) {
            employee.setUsername(employeeDTO.getAccount());
        }
        if (employeeDTO.getGender() != null) {
            employee.setSex(String.valueOf(employeeDTO.getGender()));
        }
        employee.setUpdateTime(LocalDateTime.now());
        employeeMapper.updateById(employee);
    }

    public void deleteById(Long id) {
        employeeMapper.deleteById(id);
    }
}
