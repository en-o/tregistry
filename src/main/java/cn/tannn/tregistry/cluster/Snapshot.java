package cn.tannn.tregistry.cluster;

import cn.tannn.tregistry.model.InstanceMeta;
import cn.tannn.tregistry.service.TRegistryService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * 数据快照
 * @see  TRegistryService
 * @author <a href="https://tannn.cn/">tnnn</a>
 * @version V1.0
 * @date 2024/4/20 下午10:26
 */
@Data
@AllArgsConstructor
public class Snapshot {

    /**
     * 注册上来的服务集
     */
   MultiValueMap<String, InstanceMeta> REGISTRY;
    /**
     * 实例时间戳  - 实例级别
     * (service + "@" + instance.toUrl(),时间戳)
     */
    Map<String, Long> TIMESTAMPS;
    /**
     * 服务的版本 - 服务级别
     */
    Map<String, Long> VERSIONS;

    /**
     * VERSIONS#value
     */
    long version;

}
