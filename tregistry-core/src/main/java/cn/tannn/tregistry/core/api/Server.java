package cn.tannn.tregistry.core.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 注册中心中服务的实例
 *
 * @author <a href="https://tannn.cn/">tnnn</a>
 * @version V1.0
 * @date 2024/4/16 下午8:20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"url"})
public class Server {

    /**
     * urk
     */
    private String url;

    /**
     * 状态
     */
    private boolean status;

    /**
     * 老大(选主)
     */
    private boolean leader;

    /**
     * 版本号
     */
    private long version;

}
