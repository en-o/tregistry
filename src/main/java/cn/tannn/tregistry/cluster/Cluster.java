package cn.tannn.tregistry.cluster;

import cn.tannn.tregistry.properties.TRegistryProperties;
import cn.tannn.tregistry.service.TRegistryService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;

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
        // 集群初始化
        initServers();
        // 探活/选主/快照同步
        new ServerHealth(this).checkServerHealth();

    }



    /**
     * 集群初始化
     */
    private void initServers() {
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
