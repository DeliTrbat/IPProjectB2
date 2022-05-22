import Connectivity.Peer;
import Exceptions.PeerDisconnectedException;

import java.io.IOException;
import java.net.URL;

public class TestConnectivitySender {

    public static void main(String[] args) {
        try {
            System.out.println("Making Peer...");
            Peer peer = new Peer();
            var localIpList = peer.getDevices();
            System.out.println("Found devices: " + localIpList);
            peer.connectDevice("192.168.100.74", 4444);
            URL resource = SenderTest.class.getClassLoader().getResource("message.txt");
            while (true) {   // Waiting for a receiver to accept the connection
                //peer.checkActiveConnections();
                if (peer.get("192.168.100.74") != null) {
                    peer.get("192.168.100.74").sendFile(resource.getPath());
                    break;
                }
            }
        } catch (IOException | InterruptedException | PeerDisconnectedException e) {
            e.printStackTrace();
        }
    }
}