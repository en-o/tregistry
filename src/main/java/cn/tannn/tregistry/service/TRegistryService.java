package cn.tannn.tregistry.service;

import cn.tannn.tregistry.model.InstanceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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
     * 注册上来的服务集
     */
    private static MultiValueMap<String, InstanceMeta> REGISTRY = new LinkedMultiValueMap<>();

    /**
     * 服务的版本 - 服务级别
     */
    final static Map<String, Long> VERSIONS = new ConcurrentHashMap<>();
    final static AtomicLong VERSION = new AtomicLong(0);

    /**
     * 实例时间戳  - 实例级别
     * (service + "@" + instance.toUrl(),时间戳)
     */
    public final static Map<String, Long> TIMESTAMPS = new ConcurrentHashMap<>();


    @Override
    public InstanceMeta register(String service, InstanceMeta instance) {
        List<InstanceMeta> metas = REGISTRY.get(service);
        if (metas != null && !metas.isEmpty()) {
            if (metas.contains(instance)) {
                // 已经注册了
                log.info("Instance {} already registered", instance.toUrl());
                instance.setStatus(true);
                return instance;
            }
        }
        log.info("===> register instance {}", instance.toUrl());
        REGISTRY.add(service, instance);
        instance.setStatus(true);
        renew(instance, service);
        // 服务添加版本，作为后续事件的处理
        VERSIONS.put(service, VERSION.incrementAndGet());
        return instance;
    }

    @Override
    public InstanceMeta unregister(String service, InstanceMeta instance) {
        List<InstanceMeta> metas = REGISTRY.get(service);
        if (metas == null || metas.isEmpty()) {
            return null;
        }
        log.info("unregister instance {}", instance.toUrl());
        metas.removeIf(meta -> meta.equals(instance));
        instance.setStatus(false);
        renew(instance, service);
        // 服务添加版本，作为后续事件的处理
        VERSIONS.put(service, VERSION.incrementAndGet());
        return instance;
    }

    @Override
    public List<InstanceMeta> getAllInstances(String service) {
        return REGISTRY.get(service);
    }


    @Override
    public Long renew(InstanceMeta instance, String... services) {
        // 探活
        long millis = System.currentTimeMillis();
        for (String service : services) {
            TIMESTAMPS.putIfAbsent(service + "@" + instance.toUrl(), millis);
        }
        return millis;
    }


    @Override
    public Long version(String service) {
        return VERSIONS.get(service);
    }

    /**
     * 服务版本
     *
     * @param service 服务名
     * @return service, version
     */
    @Override
    public Map<String, Long> version(String... service) {
        return Arrays.stream(service).collect(Collectors.toMap(x -> x, VERSIONS::get, (a, b) -> b));
    }

}
