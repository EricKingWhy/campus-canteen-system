package fun.cyhgraph.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.cyhgraph.constant.MessageConstant;
import fun.cyhgraph.constant.StatusConstant;
import fun.cyhgraph.context.BaseContext;
import fun.cyhgraph.dto.EmployeeDTO;
import fun.cyhgraph.dto.EmployeeLoginDTO;
import fun.cyhgraph.dto.EmployeePageQueryDTO;
import fun.cyhgraph.dto.EmployeeRegisterDTO;
import fun.cyhgraph.entity.Employee;
import fun.cyhgraph.exception.AccountLockedException;
import fun.cyhgraph.exception.AccountNotFoundException;
import fun.cyhgraph.exception.LoginFailedException;
import fun.cyhgraph.exception.PasswordErrorException;
import fun.cyhgraph.mapper.EmployeeMapper;
import fun.cyhgraph.result.PageResult;
import fun.cyhgraph.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, username);
        Employee employee = employeeMapper.selectOne(queryWrapper);

        if (employee == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 毕设环境：直接明文比对
        if (!password.equals(employee.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        return employee;
    }

    @Override
    public void register(EmployeeRegisterDTO employeeRegisterDTO) {
        String account = employeeRegisterDTO.getAccount();
        String password = employeeRegisterDTO.getPassword();

        if (!StringUtils.hasText(account) || !StringUtils.hasText(password)) {
            throw new LoginFailedException("账号或密码不能为空");
        }

        if (StringUtils.hasText(employeeRegisterDTO.getRepassword())
                && !password.equals(employeeRegisterDTO.getRepassword())) {
            throw new LoginFailedException("两次输入的密码不一致");
        }

        String username = account.trim();
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, username);
        Employee existing = employeeMapper.selectOne(queryWrapper);
        if (existing != null) {
            throw new LoginFailedException(username + MessageConstant.ALREADY_EXIST);
        }

        LocalDateTime now = LocalDateTime.now();
        Long operatorId = BaseContext.getCurrentId() == null ? 1L : BaseContext.getCurrentId();

        Employee employee = new Employee();
        employee.setUsername(username);
        employee.setName(username);
        employee.setPassword(password);
        employee.setStatus(StatusConstant.ENABLE);
        employee.setPhone("13800000000");
        employee.setGender(0);
        employee.setAge(null);
        employee.setIdNumber("11010119900101000" + new Random().nextInt(10));
        employee.setCreateTime(now);
        employee.setUpdateTime(now);
        employee.setCreateUser(operatorId);
        employee.setUpdateUser(operatorId);

        try {
            employeeMapper.insert(employee);
        } catch (DataIntegrityViolationException ex) {
            if (isSqlIntegrityConstraintViolation(ex)) {
                throw new LoginFailedException(username + MessageConstant.ALREADY_EXIST);
            }
            throw new LoginFailedException("管理员注册失败，请检查提交信息");
        }
    }

    private boolean isSqlIntegrityConstraintViolation(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SQLIntegrityConstraintViolationException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        employee.setUsername(employeeDTO.getAccount());

        if (employee.getIdNumber() == null || employee.getIdNumber().isEmpty()) {
            employee.setIdNumber("11010119900101000" + new Random().nextInt(10));
        }
        employee.setId(null);

        employee.setStatus(StatusConstant.ENABLE);
        employee.setPassword("123456");
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        employeeMapper.insert(employee);
    }

    @Override
    public PageResult pageQuery(EmployeePageQueryDTO dto) {
        Page<Employee> page = new Page<>(dto.getPage(), dto.getPageSize());
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        if (dto.getName() != null && !dto.getName().isEmpty()) {
            queryWrapper.like(Employee::getName, dto.getName());
        }
        queryWrapper.orderByDesc(Employee::getCreateTime);
        Page<Employee> p = employeeMapper.selectPage(page, queryWrapper);
        int currentYear = java.time.LocalDate.now().getYear();
        for (Employee emp : p.getRecords()) {
            if (emp.getIdNumber() != null && emp.getIdNumber().length() >= 14) {
                try {
                    int birthYear = Integer.parseInt(emp.getIdNumber().substring(6, 10));
                    emp.setAge(currentYear - birthYear);
                } catch (Exception ignored) {}
            }
        }
        return new PageResult(p.getTotal(), p.getRecords());
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Employee employee = new Employee();
        employee.setStatus(status);
        employee.setId(id);
        employeeMapper.updateById(employee);
    }

    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee != null) {
            employee.setPassword("****");
            if (employee.getIdNumber() != null && employee.getIdNumber().length() >= 14) {
                try {
                    int birthYear = Integer.parseInt(employee.getIdNumber().substring(6, 10));
                    employee.setAge(java.time.LocalDate.now().getYear() - birthYear);
                } catch (Exception ignored) {}
            }
        }
        return employee;
    }

    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        if (employeeDTO.getAccount() != null) {
            employee.setUsername(employeeDTO.getAccount());
        }
        employee.setUpdateTime(LocalDateTime.now());
        employeeMapper.updateById(employee);
    }

    @Override
    public void deleteById(Long id) {
        employeeMapper.deleteById(id);
    }
}
