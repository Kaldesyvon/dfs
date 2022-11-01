package dfs.task;

import java.io.Serializable;

public interface Task extends Runnable {
    void start();
    void stop();
    boolean isRunning();
}
