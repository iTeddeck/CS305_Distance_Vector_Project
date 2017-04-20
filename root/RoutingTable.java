import java.util.ArrayList;

public class RoutingTable {
    
    public ArrayList<String> outwardIP;
    public ArrayList<IPPort> whereToForward;
    public ArrayList<Integer> costToGet;
    
    public RoutingTable() {
        outwardIP = new ArrayList<String>();
        whereToForward = new ArrayList<IPPort>();
        costToGet = new ArrayList<Integer>();
    }
    
}