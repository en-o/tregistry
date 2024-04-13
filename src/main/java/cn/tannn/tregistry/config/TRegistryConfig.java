package cn.tannn.tregistry.config;

import cn.tannn.tregistry.service.RegistryService;
import cn.tannn.tregistry.service.TRegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * 配置
 *
 * @author <a href="https://tannn.cn/">tnnn</a>
 * @version V1.0
 * @date 2024/4/13 下午7:50
 */
@Configuration
@Slf4j
public class TRegistryConfig {

    @Bean
    public RegistryService registryService(){
        return new TRegistryService();
    }
}
