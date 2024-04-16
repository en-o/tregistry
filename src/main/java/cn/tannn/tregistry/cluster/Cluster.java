package cn.tannn.tregistry.cluster;

import cn.tannn.tregistry.http.HttpInvoker;
import cn.tannn.tregistry.http.OkHttpInvoker;
import cn.tannn.tregistry.properties.TRegistryProperties;
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
        // 集群节点探活失败
        this.executor.scheduleWithFixedDelay(() -> {
                    try {
                        updateServers();
                        electLeader();
                    } catch (Exception e) {
                        log.error("集群节点探活/选举失败", e);
                    }
                }
                , 10
                , 10
                , TimeUnit.SECONDS);

    }


    /**
     * 集群探活检测
     */
    private void updateServers() {
        servers.forEach(server -> {
            try {
                Server serverInfo = HttpInvoker.httpGet(server.getUrl() + "/info", Server.class);
                System.out.println(" ===>>> health check success for " + serverInfo);
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
     * 集群选主判断
     */
    private void electLeader() {
        List<Server> masters = this.servers.stream()
                .filter(Server::isStatus)
                .filter(Server::isLeader)
                .toList();
        if (masters.isEmpty()) {
            log.info("====> elect for no leader:  {}", servers);
            elect();
        } else if (masters.size() > 1) {
            log.info("====> elect for more then one leader: {}", servers);
            elect();
        } else {
            log.info("====> no need election for leader:  {}", masters.get(0));
        }

    }

    /**
     * 集群选主操作
     */
    private void elect() {
        // 1. 各个节点自己选，算法保证大家选的是一个 (当前)
        // 2. 外部分布式锁。谁拿到锁，谁是主
        // 3. 分布式一致性算法，比如 paxos, raft

        // 候选者
        Server candidate = null;
        for (Server server : servers) {
            server.setLeader(false);
            if (server.isStatus()) {
                if (candidate == null) {
                    candidate = server;
                } else {
                    if (candidate.hashCode() < server.hashCode()) {
                        candidate = server;
                    }
                }
            }
        }
        if (candidate != null) {
            candidate.setLeader(true);
            log.info("====> elect for leader: {}", candidate);
        } else {
            log.warn("====> elect failed  for leader");
        }

    }


    /**
     * 当前节点信息
     *
     * @return Server
     */
    public Server self() {
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
