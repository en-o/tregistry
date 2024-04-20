package cn.tannn.tregistry.cluster;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 注册中心集群选举
 *
 * @author <a href="https://tannn.cn/">tnnn</a>
 * @version V1.0
 * @date 2024/4/20 下午9:30
 */
@Slf4j
public class Election {

    /**
     * 注册中心集群选主
     * @param servers 注册中心集群
     */
    public void electLeader(List<Server> servers) {
        List<Server> masters = servers.stream()
                .filter(Server::isStatus)
                .filter(Server::isLeader)
                .toList();
        if (masters.isEmpty()) {
            // 无主
            log.warn("====> elect for no leader:  {}", servers);
            elect(servers);
        } else if (masters.size() > 1) {
            // 多主
            log.warn("====> elect for more then one leader: {}", servers);
            elect(servers);
        } else {
            log.debug("====> no need election for leader:  {}", masters.get(0));
        }

    }

    /**
     * 集群选主操作
     */
    private void elect(List<Server> servers) {
        // 1. 各个节点自己选，算法保证大家选的是一个 (当前)
        // 2. 外部分布式锁。谁拿到锁，谁是主
        // 3. 分布式一致性算法，比如 paxos, raft
        // 候选者
        Server candidate = null;
        for (Server server : servers) {
            // tips: 选主前先让大家身份变成一样， 预防多主存在的选主问题，保证只存在唯一主
            server.setLeader(false);
            // 获得才有资格选主
            if (server.isStatus()) {
                if (candidate == null) {
                    candidate = server;
                } else {
                    // tips: 选举 hashCode(url) 最小的节点
                    if (candidate.hashCode() > server.hashCode()) {
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

}
