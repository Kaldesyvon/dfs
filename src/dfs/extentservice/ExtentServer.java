package dfs.extentservice;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Objects;

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
    public byte[] get(String fileName) throws IOException {
        if (fileName.endsWith("/") || fileName.endsWith("\\")) {
            try {
                return Files.walk(getPath(path + fileName)).filter(Files::isRegularFile).toList().get(0).toString().getBytes();
            }
            catch (IOException e) {
                throw new IOException();
            }
        }
        else {
            return Files.readString(getPath(path + fileName)).getBytes();
        }
    }

    @Override
    public boolean put(String fileName, byte[] fileData) throws IOException {
        String filePath = this.path + fileName;

        File file = new File(filePath);

        if (fileData == null) {
            if ((fileName.endsWith("/") || fileName.endsWith("\\")) && Objects.requireNonNull(file.listFiles()).length > 0) {
                return false;
            }
            return file.delete();
        }

        if (fileName.endsWith("/") || fileName.endsWith("\\")){
            Files.createDirectory(getPath(filePath));
        }
        else {
            Files.write(getPath(filePath), fileData);
        }
        return true;

    }

    @Override
    public void stop() throws RemoteException, NotBoundException {
        registry.unbind(SERVICE_NAME);
    }

    public Path getPath(String fileName) {
        return Path.of(fileName);
    }

}
