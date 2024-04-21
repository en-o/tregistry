package cn.tannn.tregistry.server;

import cn.tannn.tregistry.core.api.Server;
import cn.tannn.tregistry.core.api.Snapshot;
import cn.tannn.tregistry.core.model.InstanceMeta;
import cn.tannn.tregistry.core.service.RegistryService;
import cn.tannn.tregistry.core.service.TRegistryService;
import cn.tannn.tregistry.server.cluster.Cluster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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
    @Autowired
    Cluster cluster;

    /**
     * 注册服务实例
     *
     * @param service  服务名
     * @param instance 服务实例
     * @return InstanceMeta
     */
    @RequestMapping("reg")
    public InstanceMeta register(@RequestParam("service") String service, @RequestBody InstanceMeta instance) {
        log.info("===> register {} @ {}", service, instance);
        checkLeader();
        return registryService.register(service, instance);
    }




    /**
     * 注销服务实例
     *
     * @param service  服务名
     * @param instance 服务实例
     * @return InstanceMeta
     */
    @RequestMapping("unreg")
    public InstanceMeta unregister(@RequestParam("service") String service, @RequestBody InstanceMeta instance) {
        log.info("===> unregister {} @ {}", service, instance);
        checkLeader();
        return registryService.unregister(service, instance);
    }


    /**
     * 获取所有服务实例
     *
     * @param service 服务名
     * @return InstanceMeta
     */
    @RequestMapping("findAll")
    public List<InstanceMeta> findAllInstances(@RequestParam("service") String service) {
        log.info("===> findAllInstances {}", service);
        return registryService.getAllInstances(service);
    }


    /**
     * 服务实例上报健康状况
     *
     * @param service 服务名
     * @return InstanceMeta
     */
    @RequestMapping("renew")
    public Long renew(@RequestParam("service") String service, @RequestBody InstanceMeta instance) {
        log.info("===> renew {} @ {} ", service, instance);
        checkLeader();
        return registryService.renew(instance, service);
    }


    /**
     * 服务实例上报健康状况
     *
     * @param services 服务名（逗号隔开）
     * @return InstanceMeta
     */
    @RequestMapping("renews")
    public Long renews(@RequestParam("service") String services, @RequestBody InstanceMeta instance) {
        log.info("===> renews {} @ {} ", services, instance);
        return registryService.renew(instance, services.split(","));
    }

    /**
     * 服务实例版本
     * @param service 服务名
     * @return version
     */
    @RequestMapping("version")
    public Long version(@RequestParam("service") String service) {
        log.info("===> version {} ", service);
        return registryService.version(service);
    }

    /**
     * 服务实例版本
     * @param services 服务名（逗号隔开）
     * @return version
     */
    @RequestMapping("versions")
    public Map<String, Long> versions(@RequestParam("service") String services) {
        log.info("===> versions {} ", services);
        return registryService.version(services.split(","));
    }


    /**
     * 当前集群节点信息
     * @return Server
     */
    @RequestMapping("info")
    public Server info() {
        Server self = cluster.self();
        log.debug("===> info : {} ", self);
        return self;
    }

    /**
     * 所有集群节点信息
     * @return Server
     */
    @RequestMapping("cluster")
    public List<Server> cluster() {
        List<Server> servers = cluster.getServers();
        log.info("===> cluster {}", servers);
        return servers;
    }

    /**
     * 获取当前集群的 leader
     * @return Server
     */
    @RequestMapping("leader")
    public Server leader() {
        Server leader = cluster.leader();
        log.info("===> leader {}", leader);
        return leader;
    }

    /**
     * 设置自己为主
     * @return  Server
     */
    @RequestMapping("/sl")
    public Server sl(){
        cluster.self().setLeader(true);
        log.info(" ===> leader: {}", cluster.self());
        return cluster.self();
    }


    /**
     * 获取集群 snapshot
     * @return Server
     */
    @RequestMapping("snapshot")
    public Snapshot snapshot() {
        Snapshot snapshot = TRegistryService.snapshot();
        log.info("===> snapshot {}", snapshot);
        return snapshot;
    }


    /**
     * 非leader不允许操作指定接口
     */
    private void checkLeader() {
        if(!cluster.self().isLeader()){
            throw new RuntimeException("current server is not a leader, the leader is " +  cluster.leader().getUrl());
        }
    }

}
