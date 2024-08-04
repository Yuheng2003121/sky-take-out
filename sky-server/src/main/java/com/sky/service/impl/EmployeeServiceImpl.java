package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对前端传来的明文密码进行MD5加密,再与数据库的该用户密码进行比对(数据库里用户密码都是MD5加密过的)
       password = DigestUtils.md5DigestAsHex(password.getBytes());//MD5加密
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /*
     * 新增员工
     * */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();

        //属性拷贝, 把empDTO对象里的属性拷贝到employee里(前提这两个类这些属性名一样)
        BeanUtils.copyProperties(employeeDTO, employee);

        //设置额外属性
        //设置账号状态
        employee.setStatus(StatusConstant.ENABLE);//1:正常 0:禁用

        //设置密码
        String password = PasswordConstant.DEFAULT_PASSWORD;
        employee.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));

        //设置当前账号创建/更新时间
        /*employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //设置当前记录创建人id和修改人id
        //把从JWT解析时候存储到线程存储中的id赋值给employee(通过treadlocal)
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());*/

        employeeMapper.insert(employee);


    }

    /*
    * 分页查询员工
    * */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //基于pagehelper

        //开始分页
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);

        long total = page.getTotal();//分页查询到的数量
        List<Employee> records = page.getResult();//分页查询到的所有员工

        return new PageResult(total, records);
    }


    /*
     * 启用/禁用员工账号(根据id修改员工status)
     * */
    @Override
    public void startOrStop(Integer status, Long id) {
        //update employee set status =? where id =?

        /*Employee employee = new Employee();
        employee.setStatus(status);
        employee.setId(id);*/
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();

        employeeMapper.update(employee);
    }


    /*
     * 查询员工信息(根据id)
     * */
    @Override
    public Employee getById(Integer id) {
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("****");

        return employee;
    }


    /*
    * 编辑员工信息
    * */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();

        //属性拷贝, 把empDTO对象里的属性拷贝到employee里(前提这两个类这些属性名一样)
        BeanUtils.copyProperties(employeeDTO, employee);

        //设置当前更新时间
        employee.setUpdateTime(LocalDateTime.now());

        //设置当前记录创建人id和修改人id
        //把从JWT解析时候存储到线程存储中的id赋值给employee(通过treadlocal)
        /*employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());*/


        employeeMapper.update(employee);
    }



}
