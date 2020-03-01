package com.midworm.zookeeper.leader.latch;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        int threadNum = 10;
        List<LeaderSelectionThread> threads = new ArrayList<>();
        for (int i = 0; i < threadNum; ++i) {
            LeaderSelectionThread thread = new LeaderSelectionThread("client-" + i);
            threads.add(thread);
        }
        for (int i = 0; i < threadNum; ++i) {
            threads.get(i).start();
        }

        Thread.sleep(5000);

        int rest = 5;
        while (rest > 0) {
            System.out.println("---------------------start stop thread---------------------");
            for (int i = 0; i < threadNum; ++i) {
                if (threads.get(i).isLeader()) {
                    threads.get(i).out();
                    System.out.println("thread " + i + " is stop");
                }
            }
            Thread.sleep(5000);
            for (int i = 0; i < threadNum; ++i) {
                if (threads.get(i).isLeader()) {
                    System.out.println(threads.get(i).getClientName() + " is leader");
                }
            }
            rest--;
            System.out.println("---------------------leader reselect-----------------------");
        }
    }
}
