package cn.tannn.tregistry.core.model;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * 注册服务的元数据
 *
 * @author <a href="https://tannn.cn/">tnnn</a>
 * @version V1.0
 * @date 2024/4/13 下午7:30
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"schema", "host", "port", "context"})
public class InstanceMeta {

    /**
     * 协议 [http]
     */
    private String schema;

    /**
     * host
     */
    private String host;

    /**
     * port
     */
    private Integer port;

    /**
     * 上下文
     */
    private String context;

    /**
     * 服务状态
     */
    private Boolean status;

    /**
     * 其他信息
     */
    private Map<String, String> parameters = new HashMap<>();

    /**
     * 基础构造 [schema://host:port/context]
     *
     * @param schema  协议 [http]
     * @param host    host
     * @param port    port
     * @param context 上下文
     */
    public InstanceMeta(String schema, String host, Integer port, String context) {
        this.schema = schema;
        this.host = host;
        this.port = port;
        this.context = context;
    }

    /**
     * 组装 注册中心 path
     *
     * @return zk[host_port]
     */
    public String toPath() {
        return String.format("%s_%d_%s", host, port, context);
    }

    /**
     * 组装基础 http基础构造 [http,host,port,null]
     *
     * @param host host
     * @param port port
     * @return InstanceMeta
     */
    public static InstanceMeta http(String host, Integer port) {
        return new InstanceMeta("http", host, port, "");
    }

    /**
     * 组装基础 http基础构造 [http,host,port,null]
     *
     * @param host host
     * @param port port
     * @return InstanceMeta
     */
    public static InstanceMeta http(String host, Integer port, String context) {
        return new InstanceMeta("http", host, port, context);
    }

    /**
     * 组装 url [context自带斜杠噢]
     *
     * @return [schema://host:port/context]
     */
    public String toUrl() {
        return String.format("%s://%s:%d/%s", schema, host, port, context);
    }


    /**
     * 追加 params
     * @param params 新增的
     * @return this
     */
    public InstanceMeta addParams(Map<String, String> params) {
        this.getParameters().putAll(params);
        return this;
    }


    /**
     * metas
     *
     * @return parameters to json str
     */
    public String toMetas() {
        return JSON.toJSONString(this.getParameters());
    }


    /**
     * url 转  InstanceMeta
     */
    public static InstanceMeta from(String url) {
        URI uri = URI.create(url);
        return new InstanceMeta(uri.getScheme(), uri.getHost(), uri.getPort(), uri.getPath().replace("/",""));
    }
}
