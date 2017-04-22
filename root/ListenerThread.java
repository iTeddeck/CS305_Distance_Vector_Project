import java.io.*;
import java.net.*;

public class ListenerThread implements Runnable {
    private int portNum;
    DatagramSocket serverSocket;
    byte[] receiveData = new byte[1024];
    public ListenerThread(int portNum, RoutingTable rTable, DatagramSocket serverSocket) {
        this.portNum = portNum;
        //Create UDP Connection
        this.serverSocket = serverSocket;
    }

    public void run() {
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            serverSocket.receive(receivePacket);
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();

            String message = new String(receivePacket.getData());

            if(!message.equals("")) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}