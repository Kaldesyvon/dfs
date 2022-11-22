package dfs.extentservice;

import java.rmi.Remote;

public interface ExtentConnector extends Remote {
    String SERVICE_NAME = "ExtentService";

    byte[] get(String fileName);

    boolean put(String fileName, byte[] fileData);

    void stop();

}
