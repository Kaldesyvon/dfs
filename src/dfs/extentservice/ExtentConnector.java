package dfs.extentservice;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ExtentConnector extends Remote {
    byte[] get(String fileName) throws RemoteException, IOException;

    boolean put(String fileName, byte[] fileData) throws RemoteException, IOException;

    void stop() throws RemoteException, NotBoundException;

}
