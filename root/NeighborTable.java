import java.util.ArrayList;

public class NeighborTable {
    
    public ArrayList<IPPort> neighborAddress;
    public ArrayList<Integer> neighborWeight;
    
    public NeighborTable() {
        neighborAddress = new ArrayList<IPPort>();
        neighborWeight = new ArrayList<Integer>();
    }
    
    public void addNewNeighbor(String IP, String Port, Integer weight) {
        IPPort newNeighbor = new IPPort(IP, Port);
        this.neighborAddress.add(newNeighbor);
        this.neighborWeight.add(weight);
    }
    
}