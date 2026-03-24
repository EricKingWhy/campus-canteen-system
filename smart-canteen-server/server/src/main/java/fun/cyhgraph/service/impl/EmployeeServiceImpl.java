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

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
    private static final String EMPLOYEE_PHOTO_PREFIX = "/static/upload/employee_photos/";
    private static final String PUBLIC_BASE_URL = "http://121.41.59.61:8081";
    private static final String LOCALHOST_BASE_URL = "http://127.0.0.1:8081";
    private static final String LOCALHOST_NAME_BASE_URL = "http://localhost:8081";
    private static final String PRODUCTION_IMAGE_ROOT_DIR = "/www/wwwroot/smartcanteen/images";
    private static final String EMPLOYEE_UPLOAD_RELATIVE_DIR = "smart-canteen-admin/src/assets/images/employee_photos";

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
        employee.setPhotoPath(resolvePhotoPath(employeeDTO));

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
            if (!StringUtils.hasText(emp.getPhotoPath())) {
                emp.setPhotoPath(normalizeLegacyPic(emp.getPic()));
            }
            emp.setPic(emp.getPhotoPath());
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
            if (!StringUtils.hasText(employee.getPhotoPath())) {
                employee.setPhotoPath(normalizeLegacyPic(employee.getPic()));
            }
            employee.setPic(employee.getPhotoPath());
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
        employee.setPhotoPath(resolvePhotoPath(employeeDTO));
        employee.setUpdateTime(LocalDateTime.now());
        employeeMapper.updateById(employee);
    }

    @Override
    public void deleteById(Long id) {
        employeeMapper.deleteById(id);
    }

    private String resolvePhotoPath(EmployeeDTO dto) {
        String rawPath = dto.getPhotoPath();
        if (!StringUtils.hasText(rawPath)) {
            rawPath = dto.getPic();
        }
        if (!StringUtils.hasText(rawPath)) {
            return null;
        }
        rawPath = stripKnownHostPrefix(rawPath);
        if (rawPath.startsWith(EMPLOYEE_PHOTO_PREFIX)) {
            return rawPath;
        }
        if (rawPath.startsWith("data:image/")) {
            return saveBase64Photo(rawPath);
        }
        return normalizeLegacyPic(rawPath);
    }

    private String saveBase64Photo(String dataUri) {
        int commaIndex = dataUri.indexOf(',');
        if (commaIndex <= 0) {
            return null;
        }
        String header = dataUri.substring(0, commaIndex);
        String base64 = dataUri.substring(commaIndex + 1);
        String extension = "png";
        int slashIndex = header.indexOf('/');
        int semicolonIndex = header.indexOf(';');
        if (slashIndex > 0 && semicolonIndex > slashIndex) {
            extension = header.substring(slashIndex + 1, semicolonIndex).toLowerCase();
            if ("jpeg".equals(extension)) {
                extension = "jpg";
            }
        }
        byte[] bytes = Base64.getDecoder().decode(base64);
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path targetDir = resolveEmployeePhotoDirectory();
        try {
            Files.createDirectories(targetDir);
            Files.write(targetDir.resolve(fileName), bytes);
        } catch (IOException e) {
            throw new RuntimeException("保存员工头像失败", e);
        }
        return EMPLOYEE_PHOTO_PREFIX + fileName;
    }

    private String normalizeLegacyPic(String picPath) {
        if (!StringUtils.hasText(picPath)) {
            return null;
        }
        String normalized = picPath.trim().replace("\\", "/");
        normalized = stripKnownHostPrefix(normalized);
        if (normalized.startsWith(EMPLOYEE_PHOTO_PREFIX)) {
            return normalized;
        }
        int idx = normalized.lastIndexOf('/');
        String fileName = idx >= 0 ? normalized.substring(idx + 1) : normalized;
        if (!StringUtils.hasText(fileName)) {
            return null;
        }
        return EMPLOYEE_PHOTO_PREFIX + fileName;
    }

    private Path resolveEmployeePhotoDirectory() {
        Path userDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        List<Path> candidates = new ArrayList<>();

        candidates.add(Paths.get(PRODUCTION_IMAGE_ROOT_DIR, "employee_photos"));
        candidates.add(userDir.resolve(EMPLOYEE_UPLOAD_RELATIVE_DIR));
        Path current = userDir;
        for (int i = 0; i < 6 && current != null; i++) {
            candidates.add(current.resolve(EMPLOYEE_UPLOAD_RELATIVE_DIR));
            current = current.getParent();
        }

        for (Path candidate : candidates) {
            Path normalized = candidate.normalize();
            Path parent = normalized.getParent();
            if (parent != null && Files.exists(parent)) {
                return normalized;
            }
        }

        return userDir.resolve(EMPLOYEE_UPLOAD_RELATIVE_DIR).normalize();
    }

    private String stripKnownHostPrefix(String rawPath) {
        String normalized = rawPath;
        if (normalized.startsWith(PUBLIC_BASE_URL)) {
            normalized = normalized.substring(PUBLIC_BASE_URL.length());
        }
        if (normalized.startsWith(LOCALHOST_BASE_URL)) {
            normalized = normalized.substring(LOCALHOST_BASE_URL.length());
        }
        if (normalized.startsWith(LOCALHOST_NAME_BASE_URL)) {
            normalized = normalized.substring(LOCALHOST_NAME_BASE_URL.length());
        }
        return normalized;
    }
}
