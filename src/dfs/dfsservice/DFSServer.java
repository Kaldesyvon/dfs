package dfs.dfsservice;

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

import dfs.extentservice.ExtentConnector;
import dfs.lockservice.LockConnector;

public class DFSServer implements DFSConnector, Serializable {
    private final LockCache lockCacheService;
    public final String OWNER_ID = this.toString();
    private final Registry registry;
    private final ExtentConnector extentServer;
    private final LockConnector lockServer;

    private final ExtentCache extentCacheServer;



    public DFSServer(final int port, final ExtentConnector extentServer, final LockConnector lockServer) throws IOException, AlreadyBoundException {
        this.registry = LocateRegistry.createRegistry(port);
        final var dfsServer = (DFSConnector) UnicastRemoteObject.exportObject(this, port);

        this.registry.bind("DFSService", dfsServer);

        this.extentCacheServer = new ExtentCacheServer(port, extentServer);
        this.lockCacheService = new LockCacheServer(port, this.extentCacheServer, lockServer);
        this.extentServer = extentServer;
        this.lockServer = lockServer;
        System.out.println("DFS Server is running");
    }

    @Override
    public List<String> dir(final String directoryName) throws RemoteException, InterruptedException, NotBoundException {
        this.lockCacheService.acquire(directoryName);
        try {
            final var dirs = this.extentServer.get(directoryName);
            final var res = new String(dirs).replace("\\", "/");
            final List<String> result = new ArrayList<>();
            result.add(res.substring(res.lastIndexOf("/") + 1));

            return result;
        } catch (final IOException e) {
            return null;
        } finally {
            this.lockCacheService.release(directoryName);
        }
    }

    @Override
    public boolean mkdir(final String directoryName) throws IOException, InterruptedException, NotBoundException {
        this.lockCacheService.acquire(directoryName);
        final var res = this.extentServer.put(directoryName, "hello".getBytes());
        this.lockCacheService.release(directoryName);
        return res;
    }

    @Override
    public boolean rmdir(final String directoryName) throws IOException, InterruptedException, NotBoundException {
        this.lockCacheService.acquire(directoryName);
        final var res = this.extentServer.put(directoryName, null);
        this.lockCacheService.release(directoryName);
        return res;
    }

    @Override
    public byte[] get(final String fileName) throws IOException, InterruptedException, NotBoundException {
        this.lockCacheService.acquire(fileName);
        final var res = this.extentServer.get(fileName);
        this.lockCacheService.release(fileName);
        return res;
    }

    @Override
    public boolean put(final String fileName, final byte[] fileData) throws IOException, InterruptedException, NotBoundException {
        this.lockCacheService.acquire(fileName);
        final var res = this.extentServer.put(fileName, fileData);
        this.lockCacheService.release(fileName);
        return res;
    }

    @Override
    public boolean delete(final String fileName) throws IOException, InterruptedException, NotBoundException {
        this.lockCacheService.acquire(fileName);
        final var res = this.extentServer.put(fileName, null);
        this.lockCacheService.release(fileName);
        return res;
    }

    @Override
    public void stop() {
        try {
            this.registry.unbind("DFSService");
            UnicastRemoteObject.unexportObject(this, true);
        } catch (final RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
    }
}
