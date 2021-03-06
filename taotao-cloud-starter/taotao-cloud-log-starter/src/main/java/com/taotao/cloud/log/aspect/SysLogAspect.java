package com.taotao.cloud.log.aspect;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.baomidou.mybatisplus.extension.api.R;
import com.taotao.cloud.auth.util.SecurityUtil;
import com.taotao.cloud.common.context.TenantContextHolder;
import com.taotao.cloud.log.event.SysLogEvent;
import com.taotao.cloud.log.properties.SysLogProperties;
import com.taotao.cloud.log.util.LogUtil;
import com.taotao.cloud.uc.api.entity.SysLog;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Objects;

/**
 * @Classname SysLogAspect
 * @Description 系统日志切面
 * @Author 李号东 im.lihaodong@gmail.comgit reset --merge
 * @Date 2019-04-22 23:52
 * @Version 1.0
 * ①切面注解得到请求数据 -> ②发布监听事件 -> ③异步监听日志入库
 */
@Slf4j
@Aspect
@AllArgsConstructor
public class SysLogAspect {

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    private SysLogProperties sysLogProperties;

    private final ApplicationEventPublisher publisher;

    /**
     * log实体类
     **/
    private final TransmittableThreadLocal<SysLog> sysLogThreadLocal = new TransmittableThreadLocal<>();

    /**
     * 事件发布是由ApplicationContext对象管控的，我们发布事件前需要注入ApplicationContext对象调用publishEvent方法完成事件发布
     *
     * @param publisher
     */
    public SysLogAspect(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /***
     * 定义controller切入点拦截规则，拦截SysLog注解的方法
     */
    @Pointcut("@annotation(com.taotao.cloud.log.annotation.SysOperateLog)")
    public void sysLogAspect() {

    }

    /***
     * 拦截控制层的操作日志
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Before(value = "sysLogAspect()")
    public void recordLog(JoinPoint joinPoint) throws Throwable {
        if (sysLogProperties.getEnabled()) {
            SysLog sysLog = new SysLog();
            sysLogThreadLocal.set(sysLog);

            HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
            sysLog.setApplicationName(applicationName);
            sysLog.setStartTime(LocalDateTime.now());
            sysLog.setRequestIp(ServletUtil.getClientIP(request));
            sysLog.setClientId(SecurityUtil.getClientId());
            sysLog.setUserId(SecurityUtil.getUserId());
            sysLog.setUserName(SecurityUtil.getUsername());
            sysLog.setActionUrl(URLUtil.getPath(request.getRequestURI()));
            sysLog.setStartTime(LocalDateTime.now());
            sysLog.setRequestMethod(request.getMethod());
            sysLog.setUa(request.getHeader("user-agent"));
            Object[] args = joinPoint.getArgs();
            sysLog.setClassPath(joinPoint.getTarget().getClass().getName());
            String name = joinPoint.getSignature().getName();
            sysLog.setActionMethod(name);

            sysLog.setParams(Arrays.toString(args));
            sysLog.setOperateType(LogUtil.getOperateType(name));
            sysLog.setDescription(LogUtil.getControllerMethodDescription(joinPoint));
        }

    }

    @AfterReturning(returning = "ret", pointcut = "sysLogAspect()")
    public void doAfterReturning(Object ret) {
        SysLog sysLog = sysLogThreadLocal.get();
        if (Objects.nonNull(sysLog)) {
            R r = Convert.convert(R.class, ret);
            if (r.getCode() == 200) {
                sysLog.setType(1);
            } else {
                sysLog.setType(2);
                sysLog.setExDetail(r.getMsg());
            }
            sysLog.setTenantId(TenantContextHolder.getTenant());
            sysLog.setFinishTime(LocalDateTime.now());
            long endTime = Instant.now().toEpochMilli();
            sysLog.setConsumingTime(endTime - sysLog.getStartTime().toInstant(ZoneOffset.of("+8")).toEpochMilli());

            publisher.publishEvent(new SysLogEvent(sysLog));
            sysLogThreadLocal.remove();
        }

    }

    @AfterThrowing(pointcut = "sysLogAspect()", throwing = "e")
    public void doAfterThrowable(Throwable e) {
        SysLog sysLog = sysLogThreadLocal.get();
        if (Objects.nonNull(sysLog)) {
            sysLog.setType(2);
            sysLog.setExDetail(LogUtil.getStackTrace(e));
            sysLog.setExDesc(e.getMessage());
            publisher.publishEvent(new SysLogEvent(sysLog));
            sysLogThreadLocal.remove();
        }

    }

}
