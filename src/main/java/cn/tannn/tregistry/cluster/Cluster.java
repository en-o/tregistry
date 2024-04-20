package cn.tannn.tregistry.cluster;

import cn.tannn.tregistry.http.HttpInvoker;
import cn.tannn.tregistry.properties.TRegistryProperties;
import cn.tannn.tregistry.service.TRegistryService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 注册中心集群
 *
 * @author <a href="https://tannn.cn/">tnnn</a>
 * @version V1.0
 * @date 2024/4/16 下午8:20
 */
@Slf4j
public class Cluster {

    private final TRegistryProperties registryProperties;


    public Cluster(TRegistryProperties registryProperties) {
        this.registryProperties = registryProperties;
    }


    /**
     * -- GETTER --
     * 所有节点
     *
     * @return Server
     */
    @Getter
    private List<Server> servers;


    /**
     * 集群探活
     */
    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    /**
     * 超时时间 5s （探活）
     */
    final long timeout = 5_000;

    /**
     * 当前host
     */
    String host;
    /**
     * 当前port
     */
    @Value("${server.port}")
    String port;

    Server MYSELF;

    /**
     * 初始化 - cluster server
     */
    public void init() {
        try {
            host = new InetUtils(new InetUtilsProperties()).findFirstNonLoopbackHostInfo().getIpAddress();
        } catch (Exception e) {
            log.warn("获取本身IP失败");
            host = "127.0.0.1";
        }
        MYSELF = new Server("http://" + host + ":" + port, true, false, -1L);
        log.info(" ===> myself =  {}", MYSELF);
        List<Server> servers = new ArrayList<>();
        // 获取配置中的所有节点
        registryProperties.getServerList().forEach(url -> {
            Server server = new Server();
            if (url.contains("localhost")) {
                url = url.replace("localhost", host);
            } else if (url.contains("127.0.0.1")) {
                url = url.replace("127.0.0.1", host);
            }
            if (url.equals(MYSELF.getUrl())) {
                // 添加自己详细信息到集群节点里去
                servers.add(MYSELF);
            } else {
                server.setUrl(url);
                server.setStatus(false);
                server.setLeader(false);
                server.setVersion(-1L);
                servers.add(server);
            }

        });

        this.servers = servers;
        // 注册中心集群处理线程
        this.executor.scheduleWithFixedDelay(() -> {
                    try {
                        // 集群节点探活
                        updateServers();
                        // 集群节点选主
                        new Election().electLeader(this.servers);
                        // 同步快照
                        syncSnapshotFormLeader();
                    } catch (Exception e) {
                        log.error("集群节点探活/选举/快照同步失败", e);
                    }
                }
                , 10
                , 10
                , TimeUnit.SECONDS);

    }


    /**
     * 同步快照 ：  节点实例数据同步, 从节点同步主节点数据
     */
    private void syncSnapshotFormLeader() {
        // 将当前节点数据替换成主节点数据, （主节做快照，从节点用快照
        Server leader = leader();
        Server self = self();
        //  非主 || 版本 < 主版本 （版本落后了要对其）
        if (!self.isLeader() && self.getVersion() < leader.getVersion()) {
            log.debug(" ===> leader version : {}, my version: {} , sync snapshot form leader : {} "
                    , leader.getVersion(), self.getVersion(), leader);
            // 拿到主节点信息
            Snapshot masterNodeSnapshot = HttpInvoker.httpGet(leader.getUrl() + "/snapshot", Snapshot.class);
            log.debug(" ===> sync snapshot: {}", masterNodeSnapshot);
            TRegistryService.restore(masterNodeSnapshot);
        }
    }


    /**
     * 注册中心集群探活
     */
    private void updateServers() {
        // 并行探活节点
        servers.stream().parallel().forEach(server -> {
            try {
                // 探活自己没有意义
                if (server.equals(MYSELF)) {
                    return;
                }
                // 探活的同时，设置探活节点本身的 活跃状态，版本，leader状态
                Server serverInfo = HttpInvoker.httpGet(server.getUrl() + "/info", Server.class);
                log.info(" ===>>> health check success for {}", serverInfo);
                if (serverInfo != null) {
                    server.setStatus(true);
                    server.setLeader(serverInfo.isLeader());
                    server.setVersion(serverInfo.getVersion());
                }
            } catch (Exception e) {
                log.warn("====> health check failed for {}", server);
                server.setStatus(false);
                server.setLeader(false);
            }
        });
    }


    /**
     * 当前节点信息
     *
     * @return Server
     */
    public Server self() {
        //  获取当前的版本
        MYSELF.setVersion(TRegistryService.VERSION.get());
        return MYSELF;
    }


    /**
     * 当前 leader 节点
     *
     * @return Server
     */
    public Server leader() {
        return this.servers.stream().filter(Server::isLeader).findFirst().orElse(null);
    }

}
