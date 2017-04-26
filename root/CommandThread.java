import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.*;
import java.net.*;

public class CommandThread implements Runnable {

    RoutingTable rTable;
    NeighborTable nTable;
    DatagramSocket serverSocket;
    public CommandThread(RoutingTable rTable, DatagramSocket serverSocket) {
        this.rTable = rTable;
        this.nTable = nTable;
        this.serverSocket = serverSocket;
    }

    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = "";
        try {
            line = reader.readLine();
        } catch (Exception e) {
            System.out.println(e);
        }

        //while line is not empty
        while( line != null && !line.equals("") )
        {
            if(line.contains("MSG")) {
                String[] lineArray = line.split(" ");
                if (lineArray.length == 4) {
                    byte[] sendData = new byte[1024];
                    // lineArray[1] = dst-ip
                    // lineArray[2] = dst-port
                    // lineArray[3] = msg

                    sendData = buildMessageString(lineArray[3], lineArray[2], lineArray[1]);
                    try {
                        InetAddress destIP = InetAddress.getByName(lineArray[1]);

                        IPPort nextHopIPPort = findNextHop(lineArray[1], lineArray[2]);
                        if(nextHopIPPort != null) {
                            String nextHopIPString = nextHopIPPort.getIP();
                            String nextHopPortString = nextHopIPPort.getPort();
                            InetAddress nextHopIP = InetAddress.getByName(nextHopIPString);
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, nextHopIP, Integer.parseInt(nextHopPortString));
                            try {
                                serverSocket.send(sendPacket);
                            } catch (IOException t) {
                                t.printStackTrace();
                            }
                        } else {
                            System.out.println("Could not find next hop");
                            return;
                        }
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Bad Message");
                }
            } else if (line.contains("CHANGE")) {
                String[] lineArray = line.split(" ");
                byte[] sendData = new byte[1024];
                //linearray[1] = dest ip
                //linearray[2] = dest port
                //line array [3] = new weight

                updatePersonalNeighborTable(lineArray[1], lineArray[2], lineArray[3]);
                String parseAbleMessage = buildString(lineArray[1],lineArray[2],lineArray[3]);
                sendData = parseAbleMessage.getBytes();
                try {
                    InetAddress destIP = InetAddress.getByName(lineArray[1]);
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, destIP, Integer.parseInt(lineArray[2]));
                    try {
                        serverSocket.send(sendPacket);
                    } catch (IOException t) {
                        t.printStackTrace();
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }

            } else if(line.contains("PRINT")) {
                System.out.print("                ");
                for(int i = 0; i < rTable.outwardIP.get(0).size(); i++) {
                    System.out.print(rTable.outwardIP.get(0).get(i).getIP() + "," + rTable.outwardIP.get(0).get(i).getPort() + "||");
                }
                System.out.println();

                for(int i = 0; i < rTable.costToGet.size(); i++) {
                    System.out.print(rTable.neighborAddresses.get(i).getIP() + "," + rTable.neighborAddresses.get(i).getPort() + "," + rTable.costToNeighbor.get(i) + "||");
                    for(int j = 0; j < rTable.costToGet.get(i).size(); j++) {
                        System.out.print(rTable.costToGet.get(i).get(j) + "               ");
                    }
                    System.out.println();
                }
            } else {
                System.out.println("Command does not exist");
            }

            try {
                line = reader.readLine();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public byte[] buildMessageString(String message, String endPort, String endIP) {
        String returnString = "";
        returnString += "[3] "+ message + " " + endIP + " " + endPort;
        return returnString.getBytes();
    }

    public IPPort findNextHop(String IP, String Port) {
        int indexOfOutward = -1;
        for(int i = 0; i < rTable.outwardIP.get(0).size(); i++) {
            if(rTable.outwardIP.get(0).get(i).getIP().equals(IP)
            && rTable.outwardIP.get(0).get(i).getPort().equals(Port)) {
                indexOfOutward = i;
                break;
            }
        }
        if(indexOfOutward != -1) {
            return rTable.whereToForward.get(indexOfOutward);
        } else {
            return null;
        }
    }

    public void updatePersonalNeighborTable(String neighborIP, String neighborPort, String newWeight) {
        int neighborIndex = -1;

        for(int i = 0; i < rTable.neighborAddresses.size(); i++) {
            if(rTable.neighborAddresses.get(i).getIP().equals(neighborIP)
            && rTable.neighborAddresses.get(i).getPort().equals(neighborPort)) {
                neighborIndex = i;
                break;
            }
        }

        if(neighborIndex != -1) {
            Integer newWeightInt = Integer.parseInt(newWeight);
            rTable.costToNeighbor.set(neighborIndex, newWeightInt);
        }
    }

    public String buildString(String neighborIP, String neighborPort, String newWeight) {
        String returnString = "";
        returnString += "[2] " + newWeight;
        return returnString;
    }
}