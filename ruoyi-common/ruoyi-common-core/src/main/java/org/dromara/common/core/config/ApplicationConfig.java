package org.dromara.common.core.config;

    import org.springframework.boot.autoconfigure.AutoConfiguration;
    import org.springframework.context.annotation.EnableAspectJAutoProxy;
    import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 程序注解配置
 *
 * @author Lion Li
 */
@AutoConfiguration
@EnableAspectJAutoProxy // 启用 AspectJ 风格（）的切面代理，支持 AOP 编程
@EnableAsync(proxyTargetClass = true) //开启异步任务注解，false 为 jdk 代理，true 为 cglib 代理
public class ApplicationConfig {

}
