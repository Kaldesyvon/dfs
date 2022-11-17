package dfs.dfsservice;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface DFSConnector extends Remote {
    String SERVICE_NAME = "DFSService";

    List<String> dir(String directoryName) throws IOException, InterruptedException, NotBoundException;

    boolean mkdir(String directoryName) throws IOException, InterruptedException, NotBoundException;

    boolean rmdir(String directoryName) throws IOException, InterruptedException, NotBoundException;

    byte[] get(String fileName) throws  IOException, InterruptedException, NotBoundException;

    boolean put(String fileName, byte[] fileData) throws  IOException, InterruptedException, NotBoundException;

    boolean delete(String fileName) throws  IOException, InterruptedException, NotBoundException;

    void stop() throws RemoteException, NotBoundException;
}
