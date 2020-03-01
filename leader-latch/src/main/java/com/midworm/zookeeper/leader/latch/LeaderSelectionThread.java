package com.midworm.zookeeper.leader.latch;

public class LeaderSelectionThread extends Thread {

    private LeaderSelection leaderSelection;

    private String clientName;

    public LeaderSelectionThread(String clientName) {
        this.clientName = clientName;
    }

    public boolean isLeader() {
        return leaderSelection.isLeader();
    }

    public String getClientName() {
        return clientName;
    }

    public void out() throws Exception {
        leaderSelection.cleanUp();
    }

    @Override
    public void run() {

        leaderSelection = new LeaderSelection("/leader", clientName);

        leaderSelection.isLeader();
    }
}
