package dfs.task;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public abstract class AbstractTask implements Task {
    private Thread thread;
    private boolean isRunning = false;

    @Override
    public void start() {
        thread = new Thread(this);
        thread.start();
        isRunning = true;
    }

    @Override
    public void stop() {
        isRunning = false;
        thread.interrupt();
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public abstract void run();
}
