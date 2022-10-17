package dfs.dfsservice;

import dfs.extentservice.ExtentConnector;
import dfs.lockservice.LockConnector;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

public class DFSServer implements DFSConnector, Serializable {
    public final String OWNER_ID = this.toString();
    private final Registry registry;
    private final ExtentConnector extentServer;
    private final LockConnector lockServer;

    private LockStatus lockStatus = LockStatus.NONE;


    public DFSServer(int port, ExtentConnector extentServer, LockConnector lockServer) throws RemoteException, AlreadyBoundException {
        registry = LocateRegistry.createRegistry(port);
        registry.bind(SERVICE_NAME, this);

        this.extentServer = extentServer;
        this.lockServer = lockServer;
    }

    @Override
    public List<String> dir(String directoryName) throws RemoteException {
        lockServer.acquire(directoryName, OWNER_ID, 0);

        try {
            var dirs = extentServer.get(directoryName);
            var res = new String(dirs).replace("\\", "/");
            List<String> result = new ArrayList<>();
            result.add(res.substring(res.lastIndexOf("/") + 1));

            return result;
        } catch (IOException e) {
            return null;
        } finally {
            lockServer.release(directoryName, OWNER_ID);
        }
    }

    @Override
    public boolean mkdir(String directoryName) throws RemoteException, IOException {
        lockServer.acquire(directoryName, OWNER_ID, 0);
        var res = extentServer.put(directoryName, "hello".getBytes());
        lockServer.release(directoryName, OWNER_ID);
        return res;
    }

    @Override
    public boolean rmdir(String directoryName) throws RemoteException, IOException {
        lockServer.acquire(directoryName, OWNER_ID, 0);
        var res = extentServer.put(directoryName, null);
        lockServer.release(directoryName, OWNER_ID);
        return res;
    }

    @Override
    public byte[] get(String fileName) throws RemoteException, IOException {
        lockServer.acquire(fileName, OWNER_ID, 0);
        var res = extentServer.get(fileName);
        lockServer.release(fileName, OWNER_ID);
        return res;
    }

    @Override
    public boolean put(String fileName, byte[] fileData) throws RemoteException, IOException {
        lockServer.acquire(fileName, OWNER_ID, 0);
        var res = extentServer.put(fileName, fileData);
        lockServer.release(fileName, OWNER_ID);
        return res;
    }

    @Override
    public boolean delete(String fileName) throws RemoteException, IOException {
        lockServer.acquire(fileName, OWNER_ID, 0);
        var res = extentServer.put(fileName, null);
        lockServer.release(fileName, OWNER_ID);
        return res;
    }

    @Override
    public void stop() throws RemoteException, NotBoundException {
        lockServer.stop();
        extentServer.stop();
        registry.unbind(SERVICE_NAME);
    }
}
