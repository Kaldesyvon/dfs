package dfs.dfsservice;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface DFSConnector extends Remote {

    List<String> dir(String directoryName) throws RemoteException;

    boolean mkdir(String directoryName) throws RemoteException;

    boolean rmdir(String directoryName) throws RemoteException;

    byte[] get(String fileName) throws RemoteException;

    boolean put(String fileName, byte[] fileData) throws RemoteException;

    boolean delete(String fileName) throws RemoteException;

    void stop() throws RemoteException;
}
