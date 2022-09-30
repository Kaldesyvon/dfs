package src.dfs.task;

public abstract class AbstractTask implements Task{
    private Thread thread;

    @Override
    public void start() {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void stop() {
        thread.interrupt();
    }

    @Override
    public boolean isRunning() {
        return thread.isAlive();
    }

    @Override
    public void run() {
        System.out.println(isRunning());
    }
}
