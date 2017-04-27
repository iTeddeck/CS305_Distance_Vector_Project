import java.io.*;
import java.net.*;
import java.util.*;

public class ListenerThread implements Runnable {
    private int portNum;
    DatagramSocket serverSocket;
    byte[] receiveData = new byte[2048];
    RoutingTable rTable;
    public ListenerThread(int portNum, RoutingTable rTable, DatagramSocket serverSocket) {
        this.portNum = portNum;
        //Create UDP Connection
        this.serverSocket = serverSocket;
        this.rTable = rTable;
    }

    public void run() {
        while(true) {
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
                        if(calculateDistanceVector()) {
                            //resend the DV
                            sendDistanceVector();
                        }
                    } else if (messageArray[0].equals("[2]")) {
                        changeNeighborTable(stringIPFrom, stringPortFrom, messageArray[1]);
                        if (calculateDistanceVector()) {
                            //resend the DV
                            sendDistanceVector();
                        }
                    } else if (messageArray[0].equals("[3]")) {
                        System.out.println(message);
                        parseMessage(messageArray, stringIPFrom, stringPortFrom);
                    } else {
                        System.out.println("Do not understand message received");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void parseMessage(String[] messageArray, String stringIPFrom, String stringPortFrom) {
        //arr[0] = [3]
        //arr[1] = message
        //arr[2] = end ip
        //arr[3] = end port
        //arr[4] = router hop 1 ip
        //arr[5] = router hop 1 port etc

        //System.out.println(messageArray[2] + " " + messageArray[3] + ".");
        //System.out.println(rTable.neighborAddresses.get(0).getIP() + " " + rTable.neighborAddresses.get(0).getPort() + ".");

        System.out.println(messageArray[2].trim());
        String[] realMessage3Arr = messageArray[3].trim().split("-");
        String realMessage3 = realMessage3Arr[0];
        System.out.println(realMessage3);
        
        if(rTable.neighborAddresses.get(0).getIP().equals(messageArray[2].trim())
        && rTable.neighborAddresses.get(0).getPort().equals(realMessage3)) {
            //means message was supposed to be sent to this router
            String returnString = "";
            returnString += messageArray[1];
            //for(int i = 4; i < messageArray.length; i+=2) {
            //    returnString += " " + messageArray[i] + ":" + messageArray[i+1];
            //}
            System.out.println(returnString);
            return;
        }

        IPPort nextHopIPPort = findNextHop(messageArray[2], realMessage3);
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
                    System.out.println("Message " + messageArray[1] + " from " + stringIPFrom + ":" + stringPortFrom + " to " + messageArray[2] + ":" + realMessage3
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

        for(int i = 1; i < distanceVector.length-1; i++) {
            String[] entryComponents = distanceVector[i].split("-");
            if(entryComponents.length > 3) {
                //entryComponents[0] == "("
                //entryCompo[1] == IP
                //entry compo[2] == Port
                //entry compo[3] == cost (String form)
                //entry comp[4] == ")"

                //finding the index of this component
                returnString += entryComponents[1] + ":" + entryComponents[2] + " " + entryComponents[3] + "\n";
                int indexOfDVEntry = -1;
                if(indexOfSender != -1) { //if -1, means that neighbor wasn't found in table
                    addDVEntryToTable(entryComponents, indexOfSender);
                } else {
                    System.out.println("Sender of DV was not found in Table");
                }
            }
        }
        System.out.println(returnString);
    }

    public void addDVEntryToTable(String[] entryComponent, int indexOfSender) {
        for(int i = 0; i < rTable.outwardIP.size(); i++) {
            //Has to be in each different "distance vector" -> each row
            boolean inRow = false;
            int indexInRow = -1;

            for(int j = 0; j < rTable.outwardIP.get(i).size(); j++) {
                if(rTable.outwardIP.get(i).get(j).getIP().equals(entryComponent[1]) 
                && rTable.outwardIP.get(i).get(j).getPort().equals(entryComponent[2])) {
                    inRow = true;
                    indexInRow = j;
                    break;
                }
            }

            if(i == indexOfSender && inRow) {
                rTable.costToGet.get(indexOfSender).set(indexInRow, Integer.parseInt(entryComponent[3]));
            } else if(i != indexOfSender && inRow) {
                //don't do anything,
            } else if (i == indexOfSender && !inRow) {
                //if it does not exist
                IPPort newIPPort = new IPPort(entryComponent[1], entryComponent[2]);
                rTable.outwardIP.get(indexOfSender).add(newIPPort);
                rTable.costToGet.get(indexOfSender).add(Integer.parseInt(entryComponent[3]));
            } else if( i !=indexOfSender && !inRow) {
                //add columns so all the different outward ips match
                IPPort newIPPort = new IPPort(entryComponent[1], entryComponent[2]);
                rTable.outwardIP.get(i).add(newIPPort);
                rTable.costToGet.get(i).add(1000000);
                
                if(i == 0) {
                    rTable.whereToForward.add(newIPPort);
                }
            }
        }
    }

    /*public boolean calculateDistanceVector() {
        boolean didChange = false;
        for(int i = 0; i < rTable.outwardIP.get(0).size();i++) {
            System.out.println("here");
            IPPort destLoc = rTable.outwardIP.get(0).get(i);

            int currentCost = rTable.costToGet.get(0).get(i);
            IPPort currentNexHop = rTable.whereToForward.get(i);

            int newCost = 10000000;
            IPPort newNextHop = null;

            for(int j = 0; j < rTable.neighborAddresses.size(); j++) {
                int costToNeighbor = rTable.costToNeighbor.get(j);

                int indexOfDestLocInNeighbor = -1;
                int costToDestThroughNeighbor = -1;
                for(int k = 0; k < rTable.outwardIP.get(j).size(); k++) {
                    if(rTable.outwardIP.get(j).get(k).getIP().equals(destLoc.getIP())
                    && rTable.outwardIP.get(j).get(k).getPort().equals(destLoc.getPort())) {
                        indexOfDestLocInNeighbor = k;
                        break;
                    }
                }
                costToDestThroughNeighbor = rTable.costToGet.get(j).get(indexOfDestLocInNeighbor) + costToNeighbor;
                if(costToDestThroughNeighbor < newCost) {
                    newCost = costToDestThroughNeighbor;
                    newNextHop = rTable.neighborAddresses.get(i);
                }
            }

            if(newCost < currentCost) {
                rTable.costToGet.get(0).set(i, newCost);
                rTable.outwardIP.get(0).set(i, newNextHop);
                didChange = true;
            }
        }

        return didChange;
    }*/
    
    public boolean calculateDistanceVector() {
        
        boolean didChange = false;
        
        for(int i = 0; i < rTable.outwardIP.get(0).size(); i++) {
            IPPort destLoc = rTable.outwardIP.get(0).get(i);
            
            int currentCost = rTable.costToGet.get(0).get(i);
            IPPort currentNextHop = rTable.whereToForward.get(i);
            
            int newCost = 10000000;
            IPPort newNextHop = null;
            
            for(int j = 0; j < rTable.neighborAddresses.size(); j++) {
                IPPort neighborLoc = rTable.neighborAddresses.get(j);
                int costToNeighbor = rTable.costToNeighbor.get(j);
                int costFromNeighborToDest = -1;
                
                int indexOfDestInNeighbor = -1;
                for(int a = 0; a < rTable.outwardIP.get(j).size(); a++) {
                    if(rTable.outwardIP.get(j).get(a).getIP().equals(destLoc.getIP())
                    && rTable.outwardIP.get(j).get(a).getPort().equals(destLoc.getPort())) {
                        indexOfDestInNeighbor = a;
                        break;
                    }
                }
                
                costFromNeighborToDest = rTable.costToGet.get(j).get(indexOfDestInNeighbor);
                
                int costThroughNeighbor = costFromNeighborToDest + costToNeighbor;
                
                if(costThroughNeighbor < newCost) {
                    newCost = costThroughNeighbor;
                    newNextHop = rTable.neighborAddresses.get(j);
                }
            }
            
            if(newCost < currentCost) {
                rTable.costToGet.get(0).set(i, newCost);
                rTable.whereToForward.set(i, newNextHop);
                didChange = true;
            }
      }
      return didChange;
        
    }

    public void sendDistanceVector() {
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