import java.util.ArrayList;

public class RoutingTable {
    
    ArrayList<String> outwardIP;
    ArrayList<IPPort> whereToForward;
    
    public RoutingTable() {
        outwardIP = new ArrayList<String>();
        whereToForward = new ArrayList<IPPort>();
    }
    
}