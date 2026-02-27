package com.rag.chatmcpservice.config;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Tool 方法异常处理的 AOP 切面
 */
@Aspect
@Component
public class ToolExceptionAspect {

    private static final Logger log = LoggerFactory.getLogger(ToolExceptionAspect.class);

    /**
     * 切点：匹配所有带有 @Tool 注解的方法
     */
    @Pointcut("@annotation(org.springframework.ai.tool.annotation.Tool)")
    public void toolMethods() {}

    /**
     * 环绕通知：捕获 Tool 方法的所有异常
     */
    @Around("toolMethods()")
    public Object handleToolException(ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();

        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            // 打印异常日志
            log.error("Tool方法 [{}] 执行异常: {}", methodName, e.getMessage(), e);

            // 返回友好的提示信息
            return "调用工具时发生异常，请使用联网查询功能尝试搜索你想的信息";
        }
    }
}