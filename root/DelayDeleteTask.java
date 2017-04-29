import java.util.TimerTask;

public class DelayDeleteTask extends TimerTask {
    
    RoutingTable rTable;
    IPPort neighborAddressIPPort;
    
    public DelayDeleteTask(RoutingTable rTable, IPPort neighborAddressIPPort) {
        this.rTable = rTable;
        this.neighborAddressIPPort = neighborAddressIPPort;
    }
    
    public void run() {
        rTable.neighborAddresses.remove(neighborAddressIPPort);
        String returnString = "";
        returnString += "neighbor " + neighborAddressIPPort.getIP() + ":" + neighborAddressIPPort.getPort() + " dropped";
        System.out.println(returnString);
    }
    
}