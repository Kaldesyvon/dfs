package src.dfs.dfsservice;

import src.dfs.extentsevice.ExtentConnector;
import src.dfs.lockservice.LockConnector;

import java.rmi.RemoteException;
import java.util.List;

public class DFSServer implements DFSConnector {

    public DFSServer(int port, ExtentConnector extentServer, LockConnector lockServer) throws RemoteException {

    }

    @Override
    public List<String> dir(String directoryName) throws RemoteException {
        return null;
    }

    @Override
    public boolean mkdir(String directoryName) throws RemoteException {
        return false;
    }

    @Override
    public boolean rmdir(String directoryName) throws RemoteException {
        return false;
    }

    @Override
    public byte[] get(String fileName) throws RemoteException {
        return new byte[0];
    }

    @Override
    public boolean put(String fileName, byte[] fileData) throws RemoteException {
        return false;
    }

    @Override
    public boolean delete(String fileName) throws RemoteException {
        return false;
    }

    @Override
    public void stop() throws RemoteException {

    }
}
