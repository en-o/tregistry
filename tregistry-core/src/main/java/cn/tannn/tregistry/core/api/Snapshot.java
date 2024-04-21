package cn.tannn.tregistry.core.api;

import cn.tannn.tregistry.core.model.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Map;

/**
 * 数据快照
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
    LinkedMultiValueMap<String, InstanceMeta> registry;


    /**
     * 实例时间戳  - 实例级别
     * (service + "@" + instance.toUrl(),时间戳)
     */
    Map<String, Long> timestamps;
    /**
     * 服务的版本 - 服务级别
     */
    Map<String, Long> versions;

    /**
     * VERSIONS#value
     */
    long version;

}
