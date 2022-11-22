package dfs.extentservice;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Objects;

public class ExtentServer implements ExtentConnector, Serializable {

    private String path;

    private Registry registry;

    public ExtentServer(final int port, final String path) {
        try {
            this.registry = LocateRegistry.createRegistry(port);
            final var stub = (ExtentConnector) UnicastRemoteObject.exportObject(this, port);
            this.registry.bind("ExtentService", stub);

            this.path = path;

            System.out.println("Extent Server is running");
        } catch (final IOException | AlreadyBoundException e){
            e.printStackTrace();
        }
    }

    @Override
    public byte[] get(final String fileName) {
        final File file = new File(this.path + "/" + fileName);
        if (fileName.endsWith("/")){
            if (!file.isDirectory()) return null;
            final File[] files = file.listFiles();
            if (files == null) return null;

            final StringBuilder name = new StringBuilder();
            for (final File f : files) {
                name.append(f.getName());
                if (f.isDirectory()) name.append("/");
                name.append("\n");
            }
            return name.toString().getBytes();
        }

        else try {
            return Files.readAllBytes(file.toPath());
        } catch (final IOException e) {
            return null;
        }
    }

    @Override
    public boolean put(final String fileName, final byte[] fileData) {
        final String filePath = this.path + fileName;

        final File file = new File(filePath);

        if (fileData == null) {
            if ((fileName.endsWith("/") || fileName.endsWith("\\")) && Objects.requireNonNull(file.listFiles()).length > 0)
                return false;
            return file.delete();
        }
        try {
            if (fileName.endsWith("/") || fileName.endsWith("\\")) Files.createDirectory(ExtentServer.getPath(filePath));
            else Files.write(ExtentServer.getPath(filePath), fileData);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return true;

    }

    @Override
    public void stop() {
        try {
            this.registry.unbind("ExtentService");
            UnicastRemoteObject.unexportObject(this, true);
        } catch (final IOException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    public static Path getPath(final String fileName) {
        return Path.of(fileName);
    }

}
