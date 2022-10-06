package dfs.dfsservice;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface DFSConnector extends Remote {

    List<String> dir(String directoryName) throws RemoteException, IOException;

    boolean mkdir(String directoryName) throws RemoteException, IOException;

    boolean rmdir(String directoryName) throws RemoteException, IOException;

    byte[] get(String fileName) throws RemoteException, IOException;

    boolean put(String fileName, byte[] fileData) throws RemoteException, IOException;

    boolean delete(String fileName) throws RemoteException, IOException;

    void stop() throws RemoteException, NotBoundException;
}
