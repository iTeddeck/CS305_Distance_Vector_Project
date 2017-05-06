import java.util.TimerTask;
import java.io.*;
import java.net.*;

public class TimerSendDVTask extends TimerTask {
    RoutingTable rTable;
    Integer portNum;
    DatagramSocket serverSocket;
    byte[] sendData = new byte[1024];
    boolean poison;
    public TimerSendDVTask(RoutingTable rTable, Integer portNum, DatagramSocket serverSocket, boolean poison) {
        this.rTable = rTable;
        this.portNum = portNum;
        this.serverSocket = serverSocket;
        this.poison = poison;
    }

    public void run() {
        if(!poison) {
            byte[] sendData = new byte[1024];
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
            distanceVector += ">";
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
        } else {
            byte[] sendData = new byte[1024];
            for(int i = 1; i < rTable.neighborAddresses.size(); i++) {
                String distanceVector = "[1] ";
                IPPort neighborLoc = rTable.neighborAddresses.get(i);
                for(int j = 0; j < rTable.outwardIP.get(0).size(); j++) {
                    boolean nextHopIs = false;
                    IPPort nextHop = rTable.whereToForward.get(j);
                    if(neighborLoc.getIP().equals(nextHop.getIP()) && neighborLoc.getPort().equals(nextHop.getPort())) {
                        nextHopIs = true;
                    }

                    distanceVector += "(-";
                    distanceVector += rTable.outwardIP.get(0).get(j).getIP();
                    distanceVector += "-";
                    distanceVector += rTable.outwardIP.get(0).get(j).getPort();
                    distanceVector += "-";
                    if(nextHopIs) {
                        distanceVector += "1000000";
                    } else {
                        distanceVector +=rTable.costToGet.get(0).get(j);
                    }
                    distanceVector += "-) ";
                }
                distanceVector += ">";
                sendData = distanceVector.getBytes();
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

}