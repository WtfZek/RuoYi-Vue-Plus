package org.dromara.common.core.factory;

import org.dromara.common.core.utils.StringUtils;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;

/**
 * yml 配置源工厂
 *
 * @author Lion Li
 */
public class YmlPropertySourceFactory extends DefaultPropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        String sourceName = resource.getResource().getFilename();   // 获取配置源文件名
        if (StringUtils.isNotBlank(sourceName) && StringUtils.endsWithAny(sourceName, ".yml", ".yaml")) {   // yml文件才处理
            YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(resource.getResource());
            factory.afterPropertiesSet();   // 手动调用初始化方法，在 Spring 容器中，afterPropertiesSet() 通常由框架自动调用。但这里手动调用，是因为这个工厂类的实例是手动创建的
            return new PropertiesPropertySource(sourceName, factory.getObject());
        }
        return super.createPropertySource(name, resource);  // 如果文件不是 YAML 格式，则调用父类的实现处理，DefaultPropertySourceFactory 默认支持加载 .properties 文件
    }

}
