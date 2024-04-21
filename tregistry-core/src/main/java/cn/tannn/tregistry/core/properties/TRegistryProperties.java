package cn.tannn.tregistry.core.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 注册中心配置
 *
 * @author <a href="https://tannn.cn/">tnnn</a>
 * @version V1.0
 * @date 2024/4/16 下午8:25
 */
@ConfigurationProperties(prefix = "tregistry")
@Component
@Getter
@Setter
@ToString
public class TRegistryProperties {

    /**
     * 注册中心集群
     */
    List<String> serverList;



}
