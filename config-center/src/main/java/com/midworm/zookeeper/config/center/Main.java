package com.midworm.zookeeper.config.center;

import java.util.Properties;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws Exception {

        ConfigCenter publisher = new ConfigCenter("disCfg");

        String fileName = "/mall-app.properties";
        Properties properties = new Properties();
        properties.put("cpu", "4core");
        properties.put("memory", "8g");
        properties.put("hardDisk", "128G");
        System.out.println("delete file");
        publisher.delete(fileName);

        System.out.println("create file: " + properties);
        publisher.create(fileName, properties);

        properties = publisher.load(fileName);
        System.out.println("load file: " + properties);

        new Thread(() -> {
            while (true) {
                try {
                    Properties properties1 = publisher.load(fileName);
                    Random random = new Random();
                    properties1.remove("hardDisk");
                    properties1.put("ssd", random.nextInt() + "G");
                    properties1.put("memory", "16G");

                    System.out.println("modify file: " + properties1);
                    publisher.modify(fileName, properties1);
                    Thread.sleep(10000L);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        ConfigCenter subscriber = new ConfigCenter("disCfg");
        new Thread(() -> {
            try {
                subscriber.watch(fileName, properties1 -> System.out.println("change data: " + properties1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        Thread.currentThread().join();
    }
}
