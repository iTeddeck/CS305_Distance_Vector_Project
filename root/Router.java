import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Router {
    static int port;
    static NeighborTable nTable;
    static DVThread dThread;
    static ListenerThread lThread;
    static RoutingTable rTable;
    static CommandThread cThread;
    public static void main(String[] args) throws Exception
    {
        cThread = new CommandThread();
        rTable = new RoutingTable();
        lThread = new ListenerThread(port);
        dThread = new DVThread();
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

        
        
        //cThread.run();
        //dThread.run();
        //lThread.run();
    }

    public static void getNeighbors(String filePath) {
        try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String[] lineArray;
            line = br.readLine();
            lineArray = line.split(" ");
            //lineArray[0] = this ip
            port = Integer.parseInt(lineArray[1]);
            while((line = br.readLine()) != null) {
                lineArray = line.split(" ");
                //lineArray[0] = neighbor ip
                //lineArray[1] = neighbor port
                //lineArray[2] = neighbor weight
                
                nTable.addNewNeighbor(lineArray[0],lineArray[1],Integer.parseInt(lineArray[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}