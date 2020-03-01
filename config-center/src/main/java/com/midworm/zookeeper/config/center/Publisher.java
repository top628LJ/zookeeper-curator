package com.midworm.zookeeper.config.center;

import java.util.Properties;

public interface Publisher {

    void create(String path, Properties properties) throws Exception;

    void modify(String path, Properties properties) throws Exception;

    void delete(String path) throws Exception;

    Properties load(String path) throws Exception;

}
