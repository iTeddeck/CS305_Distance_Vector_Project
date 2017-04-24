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
        String distanceVector = ""; //will eventually be in form of <(IP,Port,Cost),(IP,Port,Cost)...>
        for(int a = 0; a < rTable.outwardIP.size(); a++) {
            distanceVector += "[1] "; //[1] will correspond with routine DV message
            for(int i = 0; i < rTable.outwardIP.get(a).size(); i++) {
                distanceVector += "(-";
                distanceVector += rTable.outwardIP.get(a).get(i).getIP();
                distanceVector += "-";
                distanceVector += rTable.outwardIP.get(a).get(i).getPort();
                distanceVector += "-";
                distanceVector += rTable.costToGet.get(a).get(i);
                distanceVector += "-) "; //spaces to delim
            }
        }
        sendData = distanceVector.getBytes();
        for(int i = 1; i < rTable.neighborAddresses.size();i++) { //0 is you
            System.out.println("Sending a message");
            System.out.println(distanceVector);
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