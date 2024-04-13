package cn.tannn.tregistry;

import cn.tannn.tregistry.model.InstanceMeta;
import cn.tannn.tregistry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 注册中心接口
 *
 * @author <a href="https://tannn.cn/">tnnn</a>
 * @version V1.0
 * @date 2024/4/13 下午7:49
 */
@RestController
@Slf4j
public class TRegistryController {


    @Autowired
    RegistryService registryService;

    /**
     * 注册服务
     * @param service 服务名
     * @param instance 服务实例
     * @return InstanceMeta
     */
    @RequestMapping("reg")
    public InstanceMeta register(@RequestParam("service") String service, @RequestBody InstanceMeta instance){
        log.info("===> register {} @ {}", service, instance);
        return registryService.register(service, instance);
    }

    /**
     * 注销服务
     * @param service 服务名
     * @param instance 服务实例
     * @return InstanceMeta
     */
    @RequestMapping("unreg")
    public InstanceMeta unregister(@RequestParam("service") String service, @RequestBody InstanceMeta instance){
        log.info("===> unregister {} @ {}", service, instance);
        return registryService.unregister(service, instance);
    }


    /**
     * 获取所有服务实例
     * @param service 服务名
     * @return InstanceMeta
     */
    @RequestMapping("findAll")
    public List<InstanceMeta> findAllInstances(@RequestParam("service") String service){
        log.info("===> findAllInstances {}", service);
        return registryService.getAllInstances(service);
    }
}
