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
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class DFSServer implements DFSConnector, Serializable {
    private final LockCache lockCacheService;
    public final String OWNER_ID = this.toString();
    private final Registry registry;
    private final ExtentConnector extentServer;
    private final LockConnector lockServer;



    public DFSServer(int port, ExtentConnector extentServer, LockConnector lockServer) throws IOException, AlreadyBoundException {
        registry = LocateRegistry.createRegistry(port);
        var dfsServer = (DFSConnector) UnicastRemoteObject.exportObject(this, port);

        registry.bind(SERVICE_NAME, dfsServer);

        this.lockCacheService = new LockCacheService(port, extentServer, lockServer);
        this.extentServer = extentServer;
        this.lockServer = lockServer;
        System.out.println("DFS Server ready");
    }

    @Override
    public List<String> dir(String directoryName) throws RemoteException, InterruptedException, NotBoundException {
        lockCacheService.acquire(directoryName);

        try {
            var dirs = extentServer.get(directoryName);
            var res = new String(dirs).replace("\\", "/");
            List<String> result = new ArrayList<>();
            result.add(res.substring(res.lastIndexOf("/") + 1));

            return result;
        } catch (IOException e) {
            return null;
        } finally {
            lockCacheService.release(directoryName);
        }
    }

    @Override
    public boolean mkdir(String directoryName) throws IOException, InterruptedException, NotBoundException {
        lockCacheService.acquire(directoryName);
        var res = extentServer.put(directoryName, "hello".getBytes());
        lockServer.release(directoryName, OWNER_ID);
        return res;
    }

    @Override
    public boolean rmdir(String directoryName) throws IOException, InterruptedException, NotBoundException {
        lockCacheService.acquire(directoryName);
        var res = extentServer.put(directoryName, null);
        lockCacheService.release(directoryName);
        return res;
    }

    @Override
    public byte[] get(String fileName) throws IOException, InterruptedException, NotBoundException {
        lockCacheService.acquire(fileName);
        var res = extentServer.get(fileName);
        lockCacheService.release(fileName);
        return res;
    }

    @Override
    public boolean put(String fileName, byte[] fileData) throws IOException, InterruptedException, NotBoundException {
        lockCacheService.acquire(fileName);
        var res = extentServer.put(fileName, fileData);
        lockCacheService.release(fileName);
        return res;
    }

    @Override
    public boolean delete(String fileName) throws IOException, InterruptedException, NotBoundException {
        lockCacheService.acquire(fileName);
        var res = extentServer.put(fileName, null);
        lockCacheService.release(fileName);
        return res;
    }

    @Override
    public void stop() throws NotBoundException, RemoteException {
        lockServer.stop();
        extentServer.stop();
        registry.unbind(SERVICE_NAME);
    }
}
