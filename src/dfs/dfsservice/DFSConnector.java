package dfs.dfsservice;

import java.rmi.Remote;
import java.util.List;

public interface DFSConnector extends Remote {
    String SERVICE_NAME = "DFSService";

    List<String> dir(String directoryName);

    boolean mkdir(String directoryName);

    boolean rmdir(String directoryName);

    byte[] get(String fileName);

    boolean put(String fileName, byte[] fileData);

    boolean delete(String fileName);

    void stop();
}
