package cn.tannn.tregistry.service;

import cn.tannn.tregistry.model.InstanceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

/**
 * 默认注册中心实现类
 *
 * @author <a href="https://tannn.cn/">tnnn</a>
 * @version V1.0
 * @date 2024/4/13 下午7:33
 */
@Slf4j
public class TRegistryService implements RegistryService {

    /**
     * 本地缓存 - 存储的提供者[类全限定名,Provider的映射关系]
     */
    private MultiValueMap<String, InstanceMeta> REGISTRY = new LinkedMultiValueMap<>();

    @Override
    public InstanceMeta register(String service, InstanceMeta instance) {
        List<InstanceMeta> metas = REGISTRY.get(service);
        if(metas != null && !metas.isEmpty()){
            if(metas.contains(instance)){
                // 已经注册了
                log.info("Instance {} already registered", instance.toUrl());
                instance.setStatus(true);
                return instance;
            }
        }
        log.info("===> register instance {}", instance.toUrl());
        REGISTRY.add(service, instance);
        instance.setStatus(true);
        return instance;
    }

    @Override
    public InstanceMeta unregister(String service, InstanceMeta instance) {
        List<InstanceMeta> metas = REGISTRY.get(service);
        if(metas == null || metas.isEmpty()){
            return null;
        }
        log.info("unregister instance {}", instance.toUrl());
        metas.removeIf(meta -> meta.equals(instance));
        return instance;
    }

    @Override
    public List<InstanceMeta> getAllInstances(String service) {
        return REGISTRY.get(service);
    }
}
