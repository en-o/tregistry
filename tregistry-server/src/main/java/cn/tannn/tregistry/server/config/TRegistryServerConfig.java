package cn.tannn.tregistry.server.config;

import cn.tannn.tregistry.core.properties.TRegistryProperties;
import cn.tannn.tregistry.core.service.RegistryService;
import cn.tannn.tregistry.core.service.TRegistryService;
import cn.tannn.tregistry.server.cluster.Cluster;
import cn.tannn.tregistry.server.health.HealthChecker;
import cn.tannn.tregistry.server.health.THealthChecker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置
 * @author <a href="https://tannn.cn/">tnnn</a>
 * @version V1.0
 * @date 2024/4/21 下午5:40
 */
@Configuration
@Slf4j
public class TRegistryServerConfig {

    /**
     * 注册中心提供的服务功能接口
     * @return RegistryService
     */
    @Bean
    public RegistryService registryService(){
        return new TRegistryService();
    }


    /**
     * 注册中心集群处理
     * @param tRegistryProperties TRegistryProperties
     * @return Cluster
     */
    @Bean(initMethod = "init")
    public Cluster cluster(@Autowired TRegistryProperties tRegistryProperties){
        return new Cluster(tRegistryProperties);
    }

    /**
     * 实例探活
     * @param registryService RegistryService
     * @return HealthChecker
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public HealthChecker healthChecker(@Autowired RegistryService registryService){
        return new THealthChecker(registryService);
    }

}
