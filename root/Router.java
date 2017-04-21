import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Router {
    static int port;
    static NeighborTable nTable;
    static Runnable dThread;
    static Runnable lThread;
    static RoutingTable rTable;
    static Runnable cThread;
    
    public static void main(String[] args) throws Exception
    {
        rTable = new RoutingTable();
        nTable = new NeighborTable();
        
        
        Boolean usingReverse = false;
        if(args[0].equals("-reverse")) {
            //using poison reverse
            //args[1] holds router.txt
            usingReverse = true;
            getNeighbors(args[1]);
        } else {
            //not using poison reverse
            //args[0] holds router.txt info
            getNeighbors(args[0]);
        }
        
        cThread = new CommandThread(rTable);
        lThread = new ListenerThread(port, rTable);
        dThread = new DVThread(rTable, port);

        new Thread(cThread).start();
        new Thread(lThread).start();
        new Thread(dThread).start();
    }

    public static void getNeighbors(String filePath) {
        try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String[] lineArray;
            line = br.readLine();
            lineArray = line.split(" ");
            //lineArray[0] = this ip
            //lineArray[1] = this port
            port = Integer.parseInt(lineArray[1]); //must be integer because that's passed to UDP sockets
            rTable.addNeighbor(lineArray[0],lineArray[1],0);
            while((line = br.readLine()) != null) {
                lineArray = line.split(" ");
                //lineArray[0] = neighbor ip
                //lineArray[1] = neighbor port
                //lineArray[2] = neighbor weight
                
                rTable.addNeighbor(lineArray[0],lineArray[1],Integer.parseInt(lineArray[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}