package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否完成登录
 */
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")//表示所有的请求都拦截
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
         HttpServletRequest httpServletRequest=(HttpServletRequest)servletRequest;
        HttpServletResponse httpServletResponse=(HttpServletResponse)servletResponse;

       // filterChain.doFilter(httpServletRequest,httpServletResponse);
        /**
         * 过滤器具体的处理逻辑
         * 1.获取本次请求的URI
         * 2.判断本次请求是否需要处理
         * 3.如果不需要处理，则直接放行
         * 4.判断登录状态，如果已登录，则直接放行
         * 5.如果未登录则返回未登录结果
         */
        //1.获取本次请求的URI
        String requestURI = httpServletRequest.getRequestURI();
        log.info("拦截到请求：{}",httpServletRequest.getRequestURI());
        //定义不需要处理的请求路径
       String[] urls = new String[]{//放行的请求  对于/backend/index.html则不能放行 对静态资源需要放行 就使用PATH_MATCHER
            "/employee/login",
               "/employee/logout",
                "/backend/**",
              "/front/**",
               "/user/sendMsg",//移动端发送短信
               "/user/login"//移动端登录
       };
//        String[] urls = new String[]{
//                "/**"
//        };
        //2.判断本次请求是否需要处理
        boolean check = check(requestURI, urls);
        //3.如果不需要处理，则直接放行
        if(check){
            log.info("本次请求不需要处理：{}",httpServletRequest.getRequestURI());
            filterChain.doFilter(httpServletRequest,httpServletResponse);
            return;
        }
        // 4-1.判断登录状态，如果已登录，则直接放行
        if(httpServletRequest.getSession().getAttribute("employee")!=null){
            //已经登录，直接放行
            log.info("用户已登录 用户id：{}",httpServletRequest.getSession().getAttribute("employee"));
            Long empId=(Long)httpServletRequest.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
            filterChain.doFilter(httpServletRequest,httpServletResponse);
            return;
        }

        // 4.判断登录状态，如果已登录，则直接放行
        if(httpServletRequest.getSession().getAttribute("user")!=null){
            //已经登录，直接放行
            log.info("用户已登录 用户id：{}",httpServletRequest.getSession().getAttribute("user"));
            Long userId=(Long)httpServletRequest.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(httpServletRequest,httpServletResponse);
            return;
        }

        log.info("用户未登录：{}",httpServletRequest.getRequestURI());
        //5.如果未登录则返回未登录结果，通过输出流向客户端页面响应数据
        httpServletResponse.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;


    }

    /**
     * //本次请求是否需要放行
     * @param requestURI
     * @return
     */
    public boolean check(String requestURI,String[] urls){
        for (String url:
             urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true;
            }
        }
        return  false;
    }
}
