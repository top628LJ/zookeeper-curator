package com.midworm.zookeeper.config.center;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;

public class ConfigCenter implements Publisher, Subscriber {

    private String namespace;

    private CuratorFramework client;

    public ConfigCenter(String namespace) throws Exception {
        this.namespace = namespace;
        client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .sessionTimeoutMs(5000)
                .namespace(namespace)
                .build();
        client.start();
    }

    @Override
    public void create(String path, Properties properties) throws Exception {
        if (client.checkExists().forPath(path) != null) {
            delete(path);
        }
        client.create().forPath(path);
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            client.create().forPath(path + "/" + entry.getKey().toString(), entry.getValue().toString().getBytes());
        }
    }

    @Override
    public void modify(String path, Properties properties) throws Exception {
        delete(path);
        create(path, properties);
    }

    @Override
    public void delete(String path) throws Exception {
        if (client.checkExists().forPath(path) != null) {
            client.delete().deletingChildrenIfNeeded().forPath(path);
        }
    }

    @Override
    public Properties load(String path) throws Exception {
        if (client.checkExists().forPath(path) == null) {
            return null;
        }
        List<String> children = client.getChildren().forPath(path);
        Properties properties = new Properties();
        for (String child : children) {
            properties.put(child, client.getData().forPath(path + "/" + child));
        }
        return properties;
    }

    @Override
    public void watch(String path, ChangeEvent event) throws Exception {
        System.out.println("start watch: " + path);
        ScheduledThreadPoolExecutor scheduledPool = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
        scheduledPool.setRemoveOnCancelPolicy(true);
        CopyOnWriteArrayList<ScheduledFuture<?>> list = new CopyOnWriteArrayList<>();

        final PathChildrenCache cache = new PathChildrenCache(client, path, true);
        cache.getListenable().addListener((client, event1) -> {
            System.out.println("event type: " + event1.getType() + ", list size: " + list.size());

            for (ScheduledFuture<?> scheduledFuture : list) {
                if (scheduledFuture != null && !scheduledFuture.isCancelled() && !scheduledFuture.isDone()) {
                    scheduledFuture.cancel(true);
                }
            }
            long rest = 3;
            ScheduledFuture<?> future = scheduledPool.schedule(() -> {
                try {
                    Properties properties = load(path);
                    event.dataChange(properties);
                } catch (Exception e) {
                    System.out.println("load new properties failed");
                }
            }, rest, TimeUnit.SECONDS);
            list.add(future);
        });
        cache.start();
    }
}
