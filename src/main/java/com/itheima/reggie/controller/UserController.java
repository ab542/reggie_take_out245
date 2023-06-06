package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.mapper.UserMapper;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;


    /**
     * 发送手机短信验证码
     * @param user
     * @param httpSession
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession httpSession){
        //1.获取用户的电话号码
        String phone = user.getPhone();
        if(!StringUtils.isEmpty(phone)){
            //2.生成验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("验证码："+code);
            //3.将验证码通过 短信发送 调用阿里云提供的短信服务API完成发送短信
             SMSUtils.sendMessage("李盈的博客","SMS_276352438",phone,code);
            //4.将验证码保存到session
            httpSession.setAttribute(phone,code);
            return R.success("发送短信成功");
        }
        return R.error("手机发送短信失败");
    }

    /**
     * 用户通过手机验证码登录
     * @param map
     * @param httpSession
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession httpSession){
      //1.获取用户输入的电话号码和验证码
       // map.get("")
        log.info(map.toString());
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();
        //2.获取session里的验证码
        String codeCorrect = httpSession.getAttribute(phone).toString();//正确验证码
        //3.用户输入验证码和正确验证码比较
        if(codeCorrect!=null&&code.equals(codeCorrect)){//如果相同
            //4.验证通过
            //5.从user表查询此phone 如果没有则创建一个user插入
            LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(lambdaQueryWrapper);
            if(user==null){//无此用户 需创建一个并添加
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            //6.将用户保存与session
            httpSession.setAttribute("user",user.getId());
            return R.success(user);

        }
        return R.error("登录失败");
    }
}
