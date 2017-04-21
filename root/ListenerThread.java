import java.io.*;
import java.net.*;

public class ListenerThread implements Runnable {
    private int portNum;
    DatagramSocket serverSocket;
    byte[] receiveData = new byte[1024];
    public ListenerThread(int portNum, RoutingTable rTable) {
        this.portNum = portNum;
        //Create UDP Connection
        try {
            serverSocket = new DatagramSocket(portNum);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while(true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                serverSocket.receive(receivePacket);
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();

                String message = new String(receivePacket.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}