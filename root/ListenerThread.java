import java.io.*;
import java.net.*;
import java.util.*;

public class ListenerThread implements Runnable {
    private int portNum;
    DatagramSocket serverSocket;
    byte[] receiveData = new byte[1024];
    RoutingTable rTable;
    public ListenerThread(int portNum, RoutingTable rTable, DatagramSocket serverSocket) {
        this.portNum = portNum;
        //Create UDP Connection
        this.serverSocket = serverSocket;
        this.rTable = rTable;
    }

    public void run() {
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            serverSocket.receive(receivePacket);
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();

            String message = new String(receivePacket.getData());

            String stringIPFrom = IPAddress.toString();
            String stringPortFrom = Integer.toString(port);
            String[] stringIPFromArray = stringIPFrom.split("/");
            stringIPFrom = stringIPFromArray[1];
            if(!message.equals("")) {
                String[] messageArray = message.split(" ");

                if(messageArray[0].equals("[1]")) {
                    parseReceivedDV(messageArray, stringIPFrom, stringPortFrom);
                } else if (messageArray[0].equals("[2]")) {
                    changeNeighborTable(stringIPFrom, stringPortFrom, messageArray[1]);
                } else if (messageArray[0].equals("[3]")) {
                    parseMessage(messageArray, stringIPFrom, stringPortFrom);
                } else {
                    System.out.println("Do not understand message received");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parseMessage(String[] messageArray, String stringIPFrom, String stringPortFrom) {
        //arr[0] = [3]
        //arr[1] = message
        //arr[2] = end ip
        //arr[3] = end port
        //arr[4] = router hop 1 ip
        //arr[5] = router hop 1 port etc
        
        System.out.println(messageArray[2] + " " + messageArray[3] + ".");
        System.out.println(rTable.neighborAddresses.get(0).getIP() + " " + rTable.neighborAddresses.get(0).getPort() + ".");
        
        if(rTable.neighborAddresses.get(0).getIP().equals(messageArray[2].trim())
        && rTable.neighborAddresses.get(0).getPort().equals(messageArray[3].trim())) {
            //means message was supposed to be sent to this router
            String returnString = "";
            returnString += messageArray[1];
            for(int i = 4; i < messageArray.length; i+=2) {
                returnString += " " + messageArray[i] + ":" + messageArray[i+1];
            }
            System.out.println(returnString);
            return;
        }

        IPPort nextHopIPPort = findNextHop(messageArray[2], messageArray[3]);
        if(nextHopIPPort != null) {
            String nextHopIPString = nextHopIPPort.getIP();
            String nextHopPortString = nextHopIPPort.getPort();

            //building return string
            String returnString = "";
            returnString += "[3] " + messageArray[1];
            for(int i = 2; i < messageArray.length; i++) {
                returnString += " " + messageArray[i];
            }

            returnString += " " + rTable.neighborAddresses.get(0).getIP();
            returnString += " " + rTable.neighborAddresses.get(0).getPort();

            byte[] sendData = new byte[1024];
            sendData = returnString.getBytes();
            try {
                InetAddress nextHopIP = InetAddress.getByName(nextHopIPString);
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, nextHopIP, Integer.parseInt(nextHopPortString));
                try {
                    System.out.println("Message " + messageArray[1] + " from " + stringIPFrom + ":" + stringPortFrom + " to " + messageArray[2] + ":" + messageArray[3]
                     + " forwarded to " + nextHopIPString + ":" + nextHopPortString);
                    serverSocket.send(sendPacket);
                } catch (IOException t) {
                    t.printStackTrace();
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Could not find next hop");
        }
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

    public void changeNeighborTable(String ipFrom, String portFrom, String newWeight) {
        int indexOfSender = -1;

        for(int i = 0; i < rTable.neighborAddresses.size();i++) {
            if(rTable.neighborAddresses.get(i).getIP().equals(ipFrom)
            && rTable.neighborAddresses.get(i).getPort().equals(portFrom)) {

                indexOfSender = i;
                break;
            }
        }

        if(indexOfSender != -1) {
            Integer newWeightInt = Integer.valueOf(newWeight.trim());
            rTable.costToNeighbor.set(indexOfSender, newWeightInt);

            String returnString = "new weight to neighbor ";
            returnString += ipFrom + ":" + portFrom;
            returnString += " of " + newWeight;
            System.out.println(returnString);
        }
    }

    public void parseReceivedDV(String[] distanceVector, String ipFromString, String portFromString) {
        String returnString =  "new dv received from " + ipFromString + ":" + portFromString + " with the following distances" + "\n";

        //finding index of person who sent you this DV
        int indexOfSender = -1;
        for(int i = 0; i < rTable.neighborAddresses.size(); i++) {
            if(rTable.neighborAddresses.get(i).getIP().equals(ipFromString) 
            && rTable.neighborAddresses.get(i).getPort().equals(portFromString)) {
                indexOfSender = i;
                break;
            }
        }

        for(int i = 1; i < distanceVector.length; i++) {
            String[] entryComponents = distanceVector[i].split("-");
            //entryComponents[0] == ")"
            //entryCompo[1] == IP
            //entry compo[2] == Port
            //entry compo[3] == cost (String form)
            //entry comp[4] == ")"

            //finding the index of this component

            returnString += entryComponents[1] + ":" + entryComponents[2] + " " + entryComponents[3] + "\n";

            int indexOfDVEntry = -1;
            if(indexOfSender != -1) { //if -1, means that neighbor wasn't found in table
                for(int k =0; k < rTable.outwardIP.get(indexOfSender).size();k++) {
                    if(rTable.outwardIP.get(indexOfSender).get(k).getIP().equals(entryComponents[1])
                    && rTable.outwardIP.get(indexOfSender).get(k).getPort().equals(entryComponents[2])) {
                        indexOfDVEntry = k;
                    }
                }
            } else {
                System.out.println("Sender of DV was not found in Table");
            }

            boolean wasChange = false; //will tell whether or not there was change in the DV
            if(indexOfDVEntry != -1) { //if -1, means DV Entry wasn't found in neighbors outward ip table
                if(rTable.costToGet.get(indexOfSender).get(indexOfDVEntry) == Integer.parseInt(entryComponents[3])) {
                    //Might need to do something??
                } else {
                    wasChange = true;
                }
            }

            if(wasChange) {
                System.out.println("DV Received from neighbor needs to be updated");
            } 
        }

        System.out.println(returnString);
    }
    
    public boolean calculateDistanceVector() {
        
        boolean didChange = false;
        for(int i = 0; i < rTable.outwardIP.get(0).size();i++) {
            //get current cost to node
            int costToNode = rTable.costToGet.get(0).get(i);
            //get current next hop to node
            IPPort whereNextHop = rTable.whereToForward.get(i);
            
            int newCost = costToNode;
            IPPort newNextHop = null;
            
            for(int j = 0; j < rTable.neighborAddresses.size(); j++) {
                if(rTable.costToNeighbor.get(j) + rTable.costToGet.get(0).get(i) < newCost) {
                    newCost = rTable.costToNeighbor.get(j) + rTable.costToGet.get(0).get(i);
                    newNextHop = rTable.neighborAddresses.get(j);
                }
            }
            
            if(newNextHop != null) {
                //means there was a faster way to get to this node
                rTable.costToGet.get(0).set(i, newCost);
                rTable.whereToForward.set(i, newNextHop);
                
                didChange = true;
            }
        }
        
        return didChange;
    }
}