import java.util.TimerTask;

public class DelayDeleteTask extends TimerTask {

    RoutingTable rTable;
    IPPort neighborAddressIPPort;

    public DelayDeleteTask(RoutingTable rTable, IPPort neighborAddressIPPort) {
        this.rTable = rTable;
        this.neighborAddressIPPort = neighborAddressIPPort;
    }

    public void run() {
        if(!(rTable.neighborAddresses.get(0).getIP().equals(neighborAddressIPPort.getIP())
            && rTable.neighborAddresses.get(0).getPort().equals(neighborAddressIPPort.getPort()))) {
            //getting neighbor index
            int neighborIndex = -1;
            for(int i = 0; i < rTable.neighborAddresses.size(); i++) {
                if(rTable.neighborAddresses.get(i).getIP().equals(neighborAddressIPPort.getIP())
                && rTable.neighborAddresses.get(i).getPort().equals(neighborAddressIPPort.getPort())) {
                    neighborIndex = i;
                    break;
                }
            }
            rTable.neighborAddresses.remove(neighborAddressIPPort);
            if(neighborIndex != -1) {
                rTable.outwardIP.remove(neighborIndex);
            }
            String returnString = "";
            returnString += "neighbor " + neighborAddressIPPort.getIP() + ":" + neighborAddressIPPort.getPort() + " dropped";
            System.out.println(returnString);

            //finding index in other lists
            for(int i = 0; i < rTable.outwardIP.size(); i++) {
                int index = -1;
                for(int x = 0; x < rTable.outwardIP.get(i).size(); x++) {
                    if(rTable.outwardIP.get(i).get(x).getIP().equals(neighborAddressIPPort.getIP())
                    && rTable.outwardIP.get(i).get(x).getPort().equals(neighborAddressIPPort.getPort())) {
                        index = x;
                        break;
                    }
                }
                if(index != 0) {
                    rTable.outwardIP.get(i).remove(index);
                    rTable.costToGet.get(i).remove(index);
                    if(i == 0) {
                        rTable.whereToForward.remove(index);
                    }
                }
            }
        }
    }
}
