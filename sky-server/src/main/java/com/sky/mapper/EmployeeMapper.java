package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from sky_take_out.employee where username = #{username}")
    Employee getByUsername(String username);


    /*
    * 插入员工数据
    * */
    @AutoFill(value = OperationType.INSERT) //AOP切面方法使用自定义注解标注
    @Insert("insert into sky_take_out.employee (name, username, password, phone, sex, id_number, create_time, update_time, create_user, update_user) " +
            "values " +
            "(#{name},#{username},#{password},#{phone},#{sex},#{idNumber},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void insert(Employee employee);


    /*
    * 分页查询 (动态sql写在映射xml文件里)
    * */
    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);


    /*
    * 修改员工(根据主键employee动态sql修改属性)
    * */
    @AutoFill(value = OperationType.UPDATE)//AOP切面方法使用自定义注解标注
    void update(Employee employee);


    /*
     * 查询员工信息(根据id)
     * */
    @Select("select * from sky_take_out.employee where id = #{id}")
    Employee getById(Integer id);
}
