package cn.tannn.tregistry.service;

import cn.tannn.tregistry.cluster.Snapshot;
import cn.tannn.tregistry.model.InstanceMeta;

import java.util.List;
import java.util.Map;

/**
 * 注册中心接口
 *
 * @author <a href="https://tannn.cn/">tnnn</a>
 * @version V1.0
 * @date 2024/4/13 下午7:27
 */
public interface RegistryService {

    /**
     * 注册
     * @param service 服务名
     * @param instance 服务实例
     * @return register instance
     */
    InstanceMeta register(String service, InstanceMeta instance);

    /**
     * 注销
     * @param service 服务名
     * @param instance 服务实例
     * @return unregister instance
     */
    InstanceMeta unregister(String service, InstanceMeta instance);

    /**
     * 获取所有服务实例
     * @param service 服务名
     * @return InstanceMeta of List
     */
    List<InstanceMeta> getAllInstances(String service);

    /**
     * 刷新服务时间戳
     * @param services 服务名
     * @param instance 服务实例
     * @return 时间戳
     */
    Long renew(InstanceMeta instance, String... services);

    /**
     * 服务版本
     * @param service 服务名
     * @return version
     */
    Long version(String service);

    /**
     * 服务版本
     * @param service 服务名
     * @return service,version
     */
    Map<String, Long> version(String... service);


}
