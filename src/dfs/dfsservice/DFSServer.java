package dfs.dfsservice;

import dfs.extentservice.ExtentConnector;
import dfs.lockservice.LockConnector;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DFSServer implements DFSConnector, Serializable {

    private static final String SERVICE_NAME = "DFSService";
    private final Registry registry;
    private final ExtentConnector extentServer;
    private final LockConnector lockServer;


    public DFSServer(int port, ExtentConnector extentServer, LockConnector lockServer) throws RemoteException, AlreadyBoundException {
        registry = LocateRegistry.createRegistry(port);
        registry.bind(SERVICE_NAME, this);

        this.extentServer = extentServer;
        this.lockServer = lockServer;


    }

    @Override
    public List<String> dir(String directoryName) throws RemoteException, IOException {
        List<String> resultDirs = new ArrayList<>();
        var dirs = extentServer.get(directoryName);
        return resultDirs;
    }

    @Override
    public boolean mkdir(String directoryName) throws RemoteException, IOException {
        return extentServer.put(directoryName, "hello".getBytes());
    }

    @Override
    public boolean rmdir(String directoryName) throws RemoteException, IOException {
        return extentServer.put(directoryName, null);
    }

    @Override
    public byte[] get(String fileName) throws RemoteException, IOException {
        return extentServer.get(fileName);
    }

    @Override
    public boolean put(String fileName, byte[] fileData) throws RemoteException, IOException {
        return extentServer.put(fileName, fileData);
    }

    @Override
    public boolean delete(String fileName) throws RemoteException, IOException {
        return extentServer.put(fileName, null);

    }

    @Override
    public void stop() throws RemoteException, NotBoundException {
        lockServer.stop();
        extentServer.stop();
        registry.unbind(SERVICE_NAME);
    }
}
