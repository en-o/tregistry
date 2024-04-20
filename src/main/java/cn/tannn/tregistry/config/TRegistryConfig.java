package cn.tannn.tregistry.config;

import cn.tannn.tregistry.cluster.Cluster;
import cn.tannn.tregistry.health.HealthChecker;
import cn.tannn.tregistry.health.THealthChecker;
import cn.tannn.tregistry.properties.TRegistryProperties;
import cn.tannn.tregistry.service.RegistryService;
import cn.tannn.tregistry.service.TRegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    /**
     * 注册中心提供的服务功能接口
     * @return RegistryService
     */
    @Bean
    public RegistryService registryService(){
        return new TRegistryService();
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


    /**
     * 集群
     * @param tRegistryProperties TRegistryProperties
     * @return Cluster
     */
    @Bean(initMethod = "init")
    public Cluster cluster(@Autowired TRegistryProperties tRegistryProperties){
        return new Cluster(tRegistryProperties);
    }

}
