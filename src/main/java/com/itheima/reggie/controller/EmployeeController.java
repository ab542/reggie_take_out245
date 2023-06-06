package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param employee
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(@RequestBody Employee employee, HttpServletRequest httpServletRequest){
        /**
         * 1.将页面提交的密码password进行md5加密
         * 2.将页面提交的用户名username查询数据库
         * 3.如果没有查询到则返回登录失败结果
         * 4.密码对比，如果不一致则返回登录失败结果
         * 5.查看员工状态，如果为已禁用状态，则返回员工已禁用结果
         * 6.登录成功，将员工id存入Session并返回登录成功结果
         */

         // * 1.将页面提交的密码password进行md5加密
        String password = employee.getPassword();
        password= DigestUtils.md5DigestAsHex(password.getBytes());//加密
         // 2.将页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());//条件封装
        Employee emp = employeeService.getOne(queryWrapper);
        //3.如果没有查询到则返回登录失败结果
        if(emp==null){
            return R.error("登录失败");
        }
        //4.密码对比，如果不一致则返回登录失败结果
        if(!emp.getPassword().equals(password)){
            return R.error("登录失败");
        }
        // 5.查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if(emp.getStatus()==0){
            return R.error("此员工（账号）已禁用");
        }
        //6.登录成功，将员工id存入Session并返回登录成功结果
        httpServletRequest.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 员工退出
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest httpServletRequest){
        //清理session当前员工的id
        httpServletRequest.getSession().removeAttribute("employee");

        return R.success("退出成功");
    }


    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Employee employee,HttpServletRequest httpServletRequest){

        log.info("新增员工，员工信息：{}",employee.toString());
        //初始密码123456，需要md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());

        //获得当前登录用户的id
        Long empId= (Long) httpServletRequest.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);

        employeeService.save(employee);//保存进数据库
        return R.success("新增员工成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("page={}.pageSize={},name={}",page,pageSize,name);
        //构造分页构造器
        Page pageInfo =new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(!StringUtils.isEmpty(name),Employee::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 修改员工操作
     * @param httpServletRequest
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest httpServletRequest,@RequestBody Employee employee){
        log.info(employee.toString());
        Long empId = (Long) httpServletRequest.getSession().getAttribute("employee");
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(empId);
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息。。。");
        Employee employee =employeeService.getById(id);
        if(employee!=null){
          return   R.success(employee);
        }
        return R.error("没有查询到对应员工信息");
    }
}
