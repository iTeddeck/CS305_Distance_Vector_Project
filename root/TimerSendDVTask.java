import java.util.TimerTask;
import java.io.*;
import java.net.*;

public class TimerSendDVTask extends TimerTask {
    RoutingTable rTable;
    Integer portNum;
    DatagramSocket serverSocket;
    byte[] sendData = new byte[1024];
    public TimerSendDVTask(RoutingTable rTable, Integer portNum, DatagramSocket serverSocket) {
        this.rTable = rTable;
        this.portNum = portNum;
        this.serverSocket = serverSocket;
    }

    public void run() {
        String distanceVector = "[1] "; //will eventually be in form of <(IP,Port,Cost),(IP,Port,Cost)...>
        for(int i = 0; i < rTable.outwardIP.get(0).size(); i++) {
            distanceVector += "(-";
            distanceVector += rTable.outwardIP.get(0).get(i).getIP();
            distanceVector += "-";
            distanceVector += rTable.outwardIP.get(0).get(i).getPort();
            distanceVector += "-";
            distanceVector += rTable.costToGet.get(0).get(i);
            distanceVector += "-) "; //spaces to delim
        }

        sendData = distanceVector.getBytes();
        for(int i = 1; i < rTable.neighborAddresses.size();i++) { //0 is you
            try {
                InetAddress ip = InetAddress.getByName(rTable.neighborAddresses.get(i).getIP());
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, Integer.parseInt(rTable.neighborAddresses.get(i).getPort()));
                try {
                    serverSocket.send(sendPacket);
                } catch (IOException t) {
                    t.printStackTrace();
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }
}