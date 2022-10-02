package dfs.extentservice;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ExtentConnector extends Remote {
    byte[] get(String fileName) throws RemoteException;

    boolean put(String fileName, byte[] fileData) throws RemoteException;

    void stop() throws RemoteException;
}
