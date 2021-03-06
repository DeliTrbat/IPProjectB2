package connectivity;

import connectivity.broadcast.Broadcast;
import connectivity.connection.Connection;
import connectivity.connection.ConnectionsManager;
import connectivity.connection.LocalConnection;
import connectivity.exceptions.BroadcastFailedException;
import connectivity.exceptions.DeviceAlreadyConnectedException;
import connectivity.exceptions.DeviceConnectException;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;
import java.nio.file.Path;
import java.util.*;

/**
 * This class is used to simulate a client and is the main class of Connectivity
 * Module
 */
public class Peer implements Closeable {
    /**
     * The variable is implementing the ConnectionManager, and it will be used to
     * manage all the connection a client
     * (Peer) receives
     */
    private final ConnectionsManager connectionsManager;
    /**
     * This variable is of type Broadcast, and it will be used to manage the
     * simulated broadcast through our local network
     */
    private final Broadcast broadcast;

    /**
     * The constructor starts a new broadcast through local network, at the given
     * port, with a period of sending data of
     * 5 seconds. In the same time, a new instance of connection manager is created
     * and a new thread is created in order
     * to keep a track of all connections of that client
     *
     * @param port The port value of a client through which every connection will be
     *             made
     * @throws PortUnreachableException
     * @throws SocketException
     */
    public Peer(int port) throws PortUnreachableException, SocketException {
        if (!portIsAvailable(port))
            throw new PortUnreachableException("port " + port + " is not available");
        this.broadcast = new Broadcast(port, 5);
        connectionsManager = ConnectionsManager.getInstance(port);
        new Thread(connectionsManager).start();
    }

    /**
     * This method get a set of addresses that can be reached from the client who is
     * calling this method
     *
     * @return Available devices after listening to broadcasts.
     * @throws BroadcastFailedException Broadcasting is not working
     */
    public Set<InetAddress> findDevices() throws BroadcastFailedException {
        return broadcast.getAddresses(5);
    }

    /**
     * The method is used to get an array list of active connections
     *
     * @return A list of active connections
     */
    public List<Connection> getConnectedDevices() {
        return new ArrayList<>(connectionsManager.getConnections());
    }

    /**
     * This method search through all available connections and when a connection is
     * sending a file, a new map entry in
     * returned, having as key that connection, and as value the path to that file
     *
     * @param to A path to the new received file
     * @return A map entry having as key a connection, and as value the path to a
     *         new received file
     */
    public synchronized Map.Entry<Connection, Path> incomingFile(Path to) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                for (Connection connection : connectionsManager.getConnections()) {
                    if(connection.isClosed())
                        disconnectDevice(connection);
                    try {
                        Path path = connection.receiveFile(to);
                        if (path != null)
                            return new AbstractMap.SimpleImmutableEntry<>(connection, path);
                    } catch (IOException e) {
                        return new AbstractMap.SimpleImmutableEntry<>(connection, null);
                    }
                }
                wait(5000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        return null;
    }

    /**
     * This method is adding a new connection to the list of current active
     * connections
     *
     * @param device The address of the new device which is wanted to be added in
     *               the list of active connections
     * @throws DeviceConnectException
     * @throws DeviceAlreadyConnectedException This is thrown if that specific
     *                                         device is
     *                                         already in the list of connections
     */
    public void connectDevice(InetAddress device) throws DeviceConnectException {
        LocalConnection connection;
        try {
            connection = new LocalConnection(device, getPort());
        } catch (IOException e) {
            throw new DeviceConnectException(e);
        }

        var connections = connectionsManager.getConnections();
        connections.stream()
                .filter(c -> c.equals(connection))
                .findFirst()
                .ifPresent(this::disconnectDevice);
        connections.add(connection);
    }

    /**
     * This method closed a connection of a device, removing the connection from the
     * list of connections and closing the
     * connection
     *
     * @param device The connection which is wanted to be closed
     */
    public void disconnectDevice(Connection device) {
        connectionsManager.getConnections().remove(device);
        try {
            device.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * This method returns the port value of the connections
     *
     * @return The port value of the server client
     */
    public int getPort() {
        return connectionsManager.getServerPort();
    }

    /**
     * This method check if a given port value is available or not, returning a
     * boolean value
     *
     * @param port The port value to be checked if is available
     * @return A boolean value checking if a given port is available or not
     */
    public static boolean portIsAvailable(int port) {
        /*
         * TODO
         * Add IllegalArgumentException without ruining the code
         * if(port<1024||port>65353){
         * throw new IllegalArgumentException("Not a valid port number : "+port);
         * }
         */
        try (ServerSocket tempSocket = new ServerSocket(port);
             DatagramSocket tempDatagram = new DatagramSocket(port)) {
            tempSocket.setReuseAddress(true);
            tempDatagram.setReuseAddress(true);
            return true;
        } catch (IOException ignored) {
        }
        return false;
    }

    /**
     * This method is used to auto close the broadcast variable and the
     * connectionManager variable
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        broadcast.close();
        // TODO: Close connectionsManager
        connectionsManager.stop();
    }
}