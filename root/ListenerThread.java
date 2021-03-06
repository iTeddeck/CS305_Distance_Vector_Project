import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Timer;

public class ListenerThread implements Runnable {
    private int portNum;
    DatagramSocket serverSocket;
    byte[] receiveData = new byte[2048];
    RoutingTable rTable;
    boolean poison;
    Timer timer;
    public ListenerThread(int portNum, RoutingTable rTable, DatagramSocket serverSocket, boolean poison) {
        this.portNum = portNum;
        //Create UDP Connection
        this.serverSocket = serverSocket;
        this.rTable = rTable;
        this.poison = poison;
        timer = new Timer();
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
                    String[] collidedMessage = message.split(">");
                    String[] messageArray = collidedMessage[0].split(" ");

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
                        parseMessage(messageArray, stringIPFrom, stringPortFrom);
                    } else if(messageArray[0].equals("[4]")) {
                        alertedNeighbors(messageArray, stringIPFrom, stringPortFrom);
                    } else {
                        System.out.println(message);
                        System.out.println("Do not understand message received");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void alertedNeighbors(String[] messageArray, String ipFrom, String portFrom) {
        boolean inNeighbor = false;
        for(int i = 1; i < rTable.neighborAddresses.size();i++) {
            if(rTable.neighborAddresses.get(i).getIP().equals(ipFrom)
            && rTable.neighborAddresses.get(i).getPort().equals(portFrom)) {
                inNeighbor = true;
            }
        }

        if(!inNeighbor) {
            //needs to add to neighbor
            IPPort newNeighbor = new IPPort(ipFrom, portFrom);
            Integer weight = Integer.parseInt(messageArray[2]);
            rTable.neighborAddresses.add(newNeighbor);
            rTable.costToNeighbor.add(weight);
            ArrayList<Integer> costToGetNeighbor = new ArrayList<Integer>();
            rTable.costToGet.add(costToGetNeighbor);
            DelayDeleteTask task = new DelayDeleteTask (rTable, newNeighbor);
            rTable.delays.add(task);
            timer.schedule(task, 10000);
            rTable.costToGet.get(0).add(weight);
            rTable.whereToForward.add(newNeighbor);
            rTable.outwardIP.get(0).add(newNeighbor);
        }
    }

    public void alertNeighbors() {
        for(int i = 1; i < rTable.neighborAddresses.size(); i++) {
            String sendString = "[4] " + rTable.costToNeighbor.get(i);
            sendString += " >";

            byte[] sendData = new byte[1024];
            sendData = sendString.getBytes();

            try {
                InetAddress destIP = InetAddress.getByName(rTable.neighborAddresses.get(i).getIP());
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, destIP, Integer.parseInt(rTable.neighborAddresses.get(i).getPort()));

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

    public void parseMessage(String[] messageArray,String stringIPFrom, String stringPortFrom) {
        //arr[0] = [3]
        //arr[1] = message
        //arr[2] = endip
        //arr[3] = endport
        //arr[4] = beg IP
        //arr[5] = beg port
        //arr[6] = router hop 1 ip
        //arr[7] = routerhop 1 port.. etc
        //Somewhere in ,5,7,etc there will be a / and it will collie with a different message
        ArrayList<String> messageArrayList = parseCollidedMessage(messageArray);
        String returnString = "";
        String sendString = "";
        if(messageArrayList.get(2).equals(rTable.neighborAddresses.get(0).getIP())
        && messageArrayList.get(3).equals(rTable.neighborAddresses.get(0).getPort())) {
            returnString += "Message: ";
            returnString += messageArrayList.get(1) + "\n";
            for(int i = 2; i < messageArrayList.size(); i++) {
                returnString += " " + messageArrayList.get(i);
            }
            System.out.println(returnString);
            return;
        } else {
            sendString += "[3] ";
            sendString += messageArrayList.get(1) + " " + messageArrayList.get(2) + " " + messageArrayList.get(3);
            sendString += " " + messageArrayList.get(4) + " " + messageArrayList.get(5);
            for(int i = 6; i < messageArrayList.size(); i++) {
                sendString += " " + messageArrayList.get(i);
            }
            returnString += "Message: ";
            returnString += messageArrayList.get(1) + " from " + messageArrayList.get(4) + " " + ":" + messageArrayList.get(5);
            returnString += " to " + messageArrayList.get(2) + ":" + messageArrayList.get(3);

            IPPort toForward = null;
            int index= -1;

            for(int i = 0; i < rTable.outwardIP.get(0).size(); i++) {
                if(rTable.outwardIP.get(0).get(i).getIP().equals(messageArrayList.get(2))
                && rTable.outwardIP.get(0).get(i).getPort().equals(messageArrayList.get(3))) {
                    index = i;
                }
            }

            if(index != -1) {
                toForward = rTable.whereToForward.get(index);
            }

            returnString += " forwarded to " + toForward.getIP() + ":" + toForward.getPort();
            System.out.println(returnString);
            byte[] sendData = new byte[1024];
            sendString += ">";
            sendData = sendString.getBytes();
            try {
                InetAddress nextHopIP = InetAddress.getByName(toForward.getIP());
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, nextHopIP, Integer.parseInt(toForward.getPort()));
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

    public ArrayList<String> parseCollidedMessage(String[] messageArray) {
        ArrayList<String> outputArray = new ArrayList<String>();
        for(int i = 0; i < messageArray.length; i++) {
            if(messageArray[i].contains("-")) {
                String[] collidedIndexArray = messageArray[i].split("-");
                outputArray.add(parseCollidedIndex(outputArray, collidedIndexArray[0]));
                return outputArray;
            }
            outputArray.add(messageArray[i]);
        }
        return outputArray;
    }

    public String parseCollidedIndex(ArrayList<String> messageArray, String collidedMessage) {
        return collidedMessage.substring(0, messageArray.get(3).length());
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

            DelayDeleteTask task = rTable.delays.get(indexOfSender);
            task.cancel();
            //rTable.delays.remove(indexOfSender);

            DelayDeleteTask newTask = new DelayDeleteTask(rTable, rTable.neighborAddresses.get(indexOfSender));
            rTable.delays.set(indexOfSender, newTask);
            timer.schedule(newTask, 10000);

            String returnString = "new weight to neighbor ";
            returnString += ipFrom + ":" + portFrom;
            returnString += " of " + newWeight;
            System.out.println(returnString);
        }

        //if the current path to the neighbor contains the neighbor
        //adjust the weight to inf

        int indexForward = -1;
        for(int i = 0; i < rTable.outwardIP.get(0).size(); i++) {
            if(rTable.outwardIP.get(0).get(i).getIP().equals(ipFrom)
            && rTable.outwardIP.get(0).get(i).getPort().equals(portFrom)) {
                indexForward = i;
                break;
            }
        }

        for(int x = 0; x < rTable.whereToForward.size(); x++) {
            if(rTable.whereToForward.get(x).getIP().equals(ipFrom)
            && rTable.whereToForward.get(x).getPort().equals(portFrom)) {
                rTable.costToGet.get(0).set(x, 10000);
            }
        }

        if(rTable.whereToForward.get(indexForward).getIP().equals(ipFrom)
        && rTable.whereToForward.get(indexForward).getPort().equals(portFrom)) {
            rTable.costToGet.get(0).set(indexForward,Integer.parseInt(newWeight));
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

        if(indexOfSender != -1) {

            DelayDeleteTask task = rTable.delays.get(indexOfSender);
            task.cancel();
            //rTable.delays.remove(indexOfSender);

            DelayDeleteTask newTask = new DelayDeleteTask(rTable, rTable.neighborAddresses.get(indexOfSender));
            rTable.delays.set(indexOfSender, newTask);
            timer.schedule(newTask, 8000);
        }

        for(int i = 1; i < distanceVector.length; i++) {
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
                try {
                    rTable.costToGet.get(indexOfSender).add(Integer.parseInt(entryComponent[3]));
                } catch (NumberFormatException nfe) {
                    System.out.println("Not a number");
                }
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
        String returnString = "new dv calculated" + "\n";

        for(int i = 0; i < rTable.outwardIP.get(0).size(); i++) {
            IPPort destLoc = rTable.outwardIP.get(0).get(i);
            returnString += destLoc.getIP() + ":" + destLoc.getPort();

            int currentCost = rTable.costToGet.get(0).get(i);
            IPPort currentNextHop = rTable.whereToForward.get(i);

            currentCost = getUpdatedCurrentCost(currentNextHop, destLoc, currentCost);
            int newCost = 10000000;
            IPPort newNextHop = null;

            for(int j = 1; j < rTable.neighborAddresses.size(); j++) {
                IPPort neighborLoc = rTable.neighborAddresses.get(j);
                int costToNeighbor = rTable.costToNeighbor.get(j);
                int costFromNeighborToDest = -1;

                int indexOfDestInNeighbor = -1;
                for(int a = 0; a < rTable.outwardIP.get(j).size(); a++) {
                    // System.out.println(destLoc.getIP() + ":" + destLoc.getPort() + " vs " + rTable.outwardIP.get(j).get(a).getIP() + ":" + rTable.outwardIP.get(j).get(a).getPort());
                    if(rTable.outwardIP.get(j).get(a).getIP().equals(destLoc.getIP())
                    && rTable.outwardIP.get(j).get(a).getPort().equals(destLoc.getPort())) {
                        indexOfDestInNeighbor = a;
                        break;
                    }
                }
                try {
                    costFromNeighborToDest = rTable.costToGet.get(j).get(indexOfDestInNeighbor);
                } catch (Exception e) {
                    costFromNeighborToDest = 0;
                }
                int costThroughNeighbor = costFromNeighborToDest + costToNeighbor;

                if(costThroughNeighbor < newCost) {
                    newCost = costThroughNeighbor;
                    newNextHop = rTable.neighborAddresses.get(j);
                }
            }
            
            int neighborCheckIndex = -1;
            for(int x = 0; x < rTable.neighborAddresses.size(); x++) {
                if(rTable.neighborAddresses.get(x).getIP().equals(destLoc.getIP())
                && rTable.neighborAddresses.get(x).getPort().equals(destLoc.getPort())){
                    neighborCheckIndex = x;
                }
            }
            
            if(neighborCheckIndex != -1 && !poison) {
                if(newCost > rTable.costToNeighbor.get(neighborCheckIndex)) {
                    newCost = rTable.costToNeighbor.get(neighborCheckIndex);
                    newNextHop = rTable.neighborAddresses.get(neighborCheckIndex);
                }
            }
            
            if(newCost <= currentCost) {
                rTable.costToGet.get(0).set(i, newCost);
                rTable.whereToForward.set(i, newNextHop);
            }
            
            if(newNextHop != currentNextHop) {
                didChange = true;
            }

            returnString += " " + rTable.costToGet.get(0).get(i) + " ";
            returnString += rTable.whereToForward.get(i).getIP() + ":" + rTable.whereToForward.get(i).getPort();
            returnString += "\n";
        }
        System.out.println(returnString);
        return didChange;

    }

    public int getUpdatedCurrentCost(IPPort nextHop, IPPort destLoc, int currentCost) {
        //get the cost to the next hop
        int indexNextHop = -1;
        for(int i = 0; i < rTable.neighborAddresses.size(); i++) {
            if(rTable.neighborAddresses.get(i).getIP().equals(nextHop.getIP()) 
            && rTable.neighborAddresses.get(i).getPort().equals(nextHop.getPort())) {
                indexNextHop = i;
                break;
            }
        }

        if(indexNextHop == -1) {
            return currentCost;
        }

        int costToNextHop = rTable.costToNeighbor.get(indexNextHop);

        //get the cost to the final dest from the next hop

        int destInNextHop = -1;

        for(int i = 0; i < rTable.outwardIP.get(indexNextHop).size();i++) {
            if(rTable.outwardIP.get(indexNextHop).get(i).getIP().equals(destLoc.getIP())
            && rTable.outwardIP.get(indexNextHop).get(i).getPort().equals(destLoc.getPort())) {
                destInNextHop = i;
                break;
            }
        }

        if(destInNextHop == -1) {
            return currentCost;
        }
        
        int costFromNextHop = rTable.costToGet.get(indexNextHop).get(destInNextHop);

        int totalNewCost = costToNextHop + costFromNextHop;
        return totalNewCost;
    }

    public void sendDistanceVector() {
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