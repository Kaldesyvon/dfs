package dfs.extentservice;

import java.io.File;
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
import java.util.Arrays;

public class ExtentServer implements ExtentConnector, Serializable {

    private final String path;
    private static final String SERVICE_NAME = "ExtentService";
    private final Registry registry;

    public ExtentServer(int port, String path) throws RemoteException, AlreadyBoundException {
        registry = LocateRegistry.createRegistry(port);
        this.path = path;
        registry.bind(SERVICE_NAME, this);
    }

    @Override
    public byte[] get(String fileName) throws RemoteException, IOException {
        if (fileName.endsWith("/")) {
            System.out.println("get dir " + fileName);
            return Files.walk(Paths.get(fileName)).toString().getBytes();
        }
        else {
            System.out.println("get file " + fileName);
            return Files.readString(Path.of(fileName)).getBytes();
        }
    }

    @Override
    public boolean put(String fileName, byte[] fileData) throws RemoteException, IOException {
        if (fileData == null) {
            Files.delete(Path.of(fileName));

            return true;
        }


        if (fileName.endsWith("/")){
            Files.createDirectories(Path.of(fileName));
            System.out.println(fileName);
            System.out.println(Path.of(fileName));
            System.out.println(Files.isRegularFile(Path.of(fileName)));
            System.out.println(Files.isDirectory(Path.of(fileName)));
            return Files.exists(Path.of(path));
        }
        else {
            Files.createFile(Path.of(fileName));
            Files.write(Path.of(fileName), fileData);
            System.out.println(fileName);
            System.out.println(Path.of(fileName));
            System.out.println(Files.isRegularFile(Path.of(fileName)));
            System.out.println(Files.isDirectory(Path.of(fileName)));
            return Files.exists(Path.of(path));
        }
    }

    @Override
    public void stop() throws RemoteException, NotBoundException {
        registry.unbind(SERVICE_NAME);
    }
}
