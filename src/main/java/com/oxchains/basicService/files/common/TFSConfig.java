package com.oxchains.basicService.files.common;
import com.taobao.common.tfs.DefaultTfsManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
/**
 * Created by xuqi on 2017/11/20.
 */
@Component
public class TFSConfig {
    @Value("${tfs.maxWaitThread}")
    private Integer maxWaitThread;
    @Value("${tfs.timeout}")
    private Integer timeout;
    @Value("${tfs.nisp}")
    private String nisp;
    @Value("${tfs.clusterIndex}")
    private char clusterIndex;
    @Value("${tfs.maxCacheItemCount}")
    private Integer maxCacheItemCount;
    @Value("${tfs.maxCacheTime}")
    private Integer maxCacheTime;
    @Value("${tfs.nameSpace}")
    private Integer nameSpace;
    @Bean
    public DefaultTfsManager getTfsManager(){
        DefaultTfsManager tfsManager = new DefaultTfsManager();
        tfsManager.setMaxWaitThread(maxWaitThread);
        tfsManager.setTimeout(timeout);
        tfsManager.setNsip(nisp);
        tfsManager.setTfsClusterIndex(clusterIndex);
        tfsManager.setMaxCacheItemCount(maxCacheItemCount);
        tfsManager.setMaxCacheTime(maxCacheTime);
        tfsManager.setNamespace(nameSpace);
        return tfsManager;
    }
}
