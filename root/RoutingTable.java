import java.util.ArrayList;
import java.util.Timer;

public class RoutingTable {

    public ArrayList<ArrayList<IPPort>> outwardIP; //synonymous with router name A,B,C,D of top row of router
    public ArrayList<IPPort> whereToForward; //Router to forward a message to get
    public ArrayList<ArrayList<Integer>> costToGet; //cost from here to other router
    public ArrayList<IPPort> neighborAddresses;
    public ArrayList<Integer> costToNeighbor;
    public ArrayList<DelayDeleteTask> delays;
    Timer timer;

    public RoutingTable() {
        outwardIP = new ArrayList<ArrayList<IPPort>>();
        whereToForward = new ArrayList<IPPort>();
        costToGet = new ArrayList<ArrayList<Integer>>();
        neighborAddresses = new ArrayList<IPPort>();
        costToNeighbor = new ArrayList<Integer>();
        delays = new ArrayList<DelayDeleteTask> ();
        timer = new Timer();

        ArrayList<Integer> ourCosts = new ArrayList<Integer>(); //our entry in the table
        ArrayList<IPPort> ourOutwardIps = new ArrayList<IPPort>();
        costToGet.add(ourCosts);
        outwardIP.add(ourOutwardIps);
    }

    public void addNeighbor(String IP, String port, Integer weight, boolean isMe) {
        IPPort neighborAddress = new IPPort(IP, port);
        outwardIP.get(0).add(neighborAddress);
        whereToForward.add(neighborAddress);
        costToGet.get(0).add(weight);

        neighborAddresses.add(neighborAddress);
        costToNeighbor.add(weight);

        ArrayList<IPPort> neighborOutwardIP = new ArrayList<IPPort>();
        ArrayList<Integer> neighborCosts = new ArrayList<Integer>();
        outwardIP.add(neighborOutwardIP);
        costToGet.add(neighborCosts);
        if(!isMe) {
            DelayDeleteTask task = new DelayDeleteTask(this, neighborAddress);
            delays.add(task);
            timer.schedule(task, 10000);
        } else {
            delays.add(null);
        }
    }
    
}