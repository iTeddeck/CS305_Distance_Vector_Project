import java.util.ArrayList;

public class RoutingTable {
    
    public ArrayList<IPPort> outwardIP; //synonymous with router name A,B,C,D of top row of router
    public ArrayList<IPPort> whereToForward; //Router to forward a message to get
    public ArrayList<ArrayList<Integer>> costToGet; //cost from here to other router
    public ArrayList<IPPort> neighborAddresses;
    public ArrayList<Integer> costToNeighbor;
    
    public RoutingTable() {
        outwardIP = new ArrayList<IPPort>();
        whereToForward = new ArrayList<IPPort>();
        costToGet = new ArrayList<ArrayList<Integer>>();
        neighborAddresses = new ArrayList<IPPort>();
        costToNeighbor = new ArrayList<Integer>();
        
        ArrayList<Integer> ourCosts = new ArrayList<Integer>(); //our entry in the table
        costToGet.add(ourCosts);
    }
    
    public void addNeighbor(String IP, String port, Integer weight) {
        IPPort neighborAddress = new IPPort(IP, port);
        outwardIP.add(neighborAddress);
        whereToForward.add(neighborAddress);
        costToGet.get(0).add(weight);
        neighborAddresses.add(neighborAddress);
        costToNeighbor.add(weight);
    }
   
}