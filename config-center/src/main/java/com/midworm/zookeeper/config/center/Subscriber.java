package com.midworm.zookeeper.config.center;

import java.util.Properties;

public interface Subscriber {

    Properties load(String path) throws Exception;

    void watch(String path, ChangeEvent event) throws Exception;

    interface ChangeEvent {
        void dataChange(Properties properties);
    }
}
