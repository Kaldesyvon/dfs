package dfs.extentservice;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ExtentConnector extends Remote {
    String SERVICE_NAME = "ExtentService";

    byte[] get(String fileName) throws IOException;

    boolean put(String fileName, byte[] fileData) throws IOException;

    void stop() throws RemoteException, NotBoundException;

}
