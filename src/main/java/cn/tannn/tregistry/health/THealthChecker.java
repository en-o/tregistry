package cn.tannn.tregistry.health;

import cn.tannn.tregistry.model.InstanceMeta;
import cn.tannn.tregistry.service.RegistryService;
import cn.tannn.tregistry.service.TRegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 默认探活实现 (服务探活
 *
 * @author <a href="https://tannn.cn/">tnnn</a>
 * @version V1.0
 * @date 2024/4/13 下午8:42
 */
@Slf4j
public class THealthChecker implements HealthChecker{

    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    final RegistryService registryService;

    /**
     * 超时时间 20s
     */
    long timeout = 20_000;

    public THealthChecker(RegistryService registryService) {
        this.registryService = registryService;
    }

    @Override
    public void start() {
        // 30s - 探活线程 执行配置
        this.executor.scheduleWithFixedDelay(() -> {
            log.info("===> health checker running ...");
            long now = System.currentTimeMillis();
            TRegistryService.TIMESTAMPS.forEach((s,t) -> {
                if((now - t) > timeout){
                    log.info("===> health checker : {} is down ", s);
                    int index = s.indexOf("@");
                    String service = s.substring(0, index);
                    String url = s.substring(index + 1);
                    InstanceMeta instance = InstanceMeta.from(url);
                    registryService.unregister(service,instance);
                    TRegistryService.TIMESTAMPS.remove(s);
                }
            });
        },10 , 10 , TimeUnit.SECONDS);

    }

    @Override
    public void stop() {
        executor.shutdown();
    }
}
