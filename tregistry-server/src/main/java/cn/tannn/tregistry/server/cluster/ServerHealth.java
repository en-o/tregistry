package cn.tannn.tregistry.server.cluster;

import cn.tannn.tregistry.core.api.Server;
import cn.tannn.tregistry.core.api.Snapshot;
import cn.tannn.tregistry.core.http.HttpInvoker;
import cn.tannn.tregistry.core.service.TRegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 集群探活
 *
 * @author <a href="https://tannn.cn/">tnnn</a>
 * @version V1.0
 * @date 2024/4/21 上午12:36
 */
@Slf4j
public class ServerHealth {


    /**
     * 集群探活
     */
    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    /**
     * 超时时间 5s （探活）
     */
    final long timeout = 5_000;


    final Cluster cluster;

    public ServerHealth(Cluster cluster) {
        this.cluster = cluster;
    }


    /**
     *  集群探活
     */
    public void checkServerHealth(){
        // 注册中心集群处理线程
        this.executor.scheduleWithFixedDelay(() -> {
                    try {
                        // 集群节点探活
                        updateServers();
                        // 集群节点选主
                        doElect();
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
     * 注册中心 - 集群选举
     */
    private void doElect() {
        new Election().electLeader(cluster.getServers());
    }


    /**
     * 同步快照 ：  节点实例数据同步, 从节点同步主节点数据
     */
    private void syncSnapshotFormLeader() {
        // 将当前节点数据替换成主节点数据, （主节做快照，从节点用快照
        Server leader = cluster.leader();
        Server self = cluster.self();
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
        List<Server> servers = cluster.getServers();
        // 并行探活节点
        servers.stream().parallel().forEach(server -> {
            try {
                // 探活自己没有意义
                if (server.equals(cluster.self())) {
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
}
