import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Router {
    static int port;
    public static void main(String[] args) throws Exception
    {
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

        CommandThread cThread = new CommandThread();
        RoutingTable rTable = new RoutingTable();
        ListenerThread lThread = new ListenerThread(port);
        DVThread dThread = new DVThread();
        
        
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}