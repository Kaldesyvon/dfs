package dfs.extentservice;

import java.rmi.RemoteException;

public class ExtentServer implements ExtentConnector{

    public ExtentServer(int port, String path) {

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
    public void stop() throws RemoteException {

    }
}
