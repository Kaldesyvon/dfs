package dfs.dfsservice;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dfs.extentservice.ExtentConnector;
import dfs.lockservice.LockConnector;

public class DFSServer implements dfs.dfsservice.DFSConnector {
    private final Registry registry;
    private final ExtentCache extentCacheServer;
    private final LockCache lockCacheServer;

    public DFSServer(final int port, final ExtentConnector extentServer, final LockConnector lockServer) throws IOException, AlreadyBoundException {
        this.registry = LocateRegistry.createRegistry(port);
        final DFSConnector dfsServer = (DFSConnector) UnicastRemoteObject.exportObject(this, port);
        this.registry.bind("DFSService", dfsServer);

        this.extentCacheServer = new ExtentCacheServer(port, extentServer);
        this.lockCacheServer = new LockCacheServer(port, this.extentCacheServer, lockServer);

    }

    @Override
    public synchronized List<String> dir(final String directoryName) throws IOException, NotBoundException, InterruptedException {

        this.lockCacheServer.acquire(directoryName);
        final var bytes = this.extentCacheServer.get(directoryName);
        this.extentCacheServer.update(directoryName);
        this.lockCacheServer.release(directoryName);
        if (bytes == null) return null;

        return new ArrayList<>(Arrays.asList(new String(bytes).split("\\r?\\n")));
    }

    @Override
    public synchronized boolean mkdir(final String directoryName) throws IOException, NotBoundException, InterruptedException {
        if (!directoryName.endsWith("/")) return false;

        this.lockCacheServer.acquire(directoryName);
        final var result = this.extentCacheServer.put(directoryName, "hello".getBytes());
        this.extentCacheServer.update(directoryName);
        this.lockCacheServer.release(directoryName);

        return result;
    }

    @Override
    public synchronized boolean rmdir(final String directoryName) throws IOException, NotBoundException, InterruptedException {

        this.lockCacheServer.acquire(directoryName);
        final var result = this.extentCacheServer.put(directoryName, null);
        this.extentCacheServer.update(directoryName);
        this.lockCacheServer.release(directoryName);

        return result;
    }

    @Override
    public synchronized byte[] get(final String fileName) throws IOException, NotBoundException, InterruptedException {
        this.lockCacheServer.acquire(fileName);
        final var result = this.extentCacheServer.get(fileName);
        this.extentCacheServer.update(fileName);
        this.lockCacheServer.release(fileName);

        return result;
    }

    @Override
    public synchronized boolean put(final String fileName, final byte[] fileData) throws IOException, NotBoundException, InterruptedException {
        if (fileData == null) return false;
        if (fileName.endsWith("/")) return false;
        this.lockCacheServer.acquire(fileName);
        final var result = this.extentCacheServer.put(fileName, fileData);
        this.extentCacheServer.update(fileName);
        this.lockCacheServer.release(fileName);

        return result;
    }

    @Override
    public synchronized boolean delete(final String fileName) throws IOException, NotBoundException, InterruptedException {
        if (fileName.endsWith("/")) return false;
        this.lockCacheServer.acquire(fileName);
        final var result = this.extentCacheServer.put(fileName, null);
        this.extentCacheServer.update(fileName);
        this.lockCacheServer.release(fileName);

        return result;
    }

    @Override
    public void stop() throws RemoteException, NotBoundException {
        this.registry.unbind("DFSService");
        UnicastRemoteObject.unexportObject(this, true);
        System.err.println("DFS Server stopped");
    }
}

