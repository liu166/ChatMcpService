package com.rag.chatmcpservice.config;

import com.alibaba.fastjson.JSON;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * ChatTool 方法调用日志切面（增强版）
 * - 支持 Mono 返回值打印内部值
 * - 保持你原来的日志风格
 */
@Aspect
@Component
public class ChatToolLogAspect {

    /**
     * 切点：匹配 com.rag.chatmcpservice.service.McpService 类下的所有方法
     */
    @Pointcut("execution(* com.rag.chatmcpservice.service.McpService.*(..))")
    public void chatToolMethods() {}

    /**
     * 环绕通知：记录方法执行前后日志
     */
    @Around("chatToolMethods()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();
        String fullMethodName = className + "." + methodName;

        // 获取方法参数
        Object[] args = joinPoint.getArgs();
        String argsStr = args.length > 0 ?
                Arrays.stream(args)
                        .map(arg -> arg != null ? String.valueOf(arg) : "null")
                        .collect(Collectors.joining(", "))
                : "无参数";

        // 记录开始日志
        System.out.println("┌────────── " + fullMethodName + " 开始执行 ──────────");
        System.out.println("│ 参数: " + argsStr);

        long startTime = System.currentTimeMillis();
        Object result;

        try {
            // 执行原方法
            result = joinPoint.proceed();

            // 如果返回值是 Mono，订阅打印内部值
            if (result instanceof Mono) {
                ((Mono<?>) result)
                        .doOnNext(value -> System.out.println("│ Mono内部值: " + value))
                        .doOnError(err -> System.out.println("│ Mono执行异常: " + err.getMessage()))
                        .subscribe();
            } else {
                System.out.println("│ 返回结果: " + (result != null ? JSON.toJSONString(result) : "null"));
            }

            long costTime = System.currentTimeMillis() - startTime;
            System.out.println("│ 执行耗时: " + costTime + " ms");
            System.out.println("└────────── " + fullMethodName + " 执行成功 ──────────");

            return result;

        } catch (Throwable t) {
            long costTime = System.currentTimeMillis() - startTime;
            System.out.println("│ 执行异常: " + t.getMessage());
            System.out.println("│ 执行耗时: " + costTime + " ms");
            System.out.println("└────────── " + fullMethodName + " 执行失败 ──────────");

            throw t;
        }
    }
}