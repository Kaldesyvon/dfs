package src.dfs.task;

public interface Task extends Runnable{
    void start();
    void stop();
    boolean isRunning();
    void run();
}
