package cn.tannn.tregistry.server.health;

/**
 * 探活
 *
 * @author <a href="https://tannn.cn/">tnnn</a>
 * @version V1.0
 * @date 2024/4/13 下午8:41
 */
public interface HealthChecker {

    /**
     * start
     */
    void start();

    /**
     * stop
     */
    void stop();
}
