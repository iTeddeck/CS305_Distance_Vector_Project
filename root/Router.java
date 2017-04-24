import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.*;
import java.net.*;

public class Router {
    static int port;
    static NeighborTable nTable;
    static Runnable dThread;
    static Runnable lThread;
    static RoutingTable rTable;
    static Runnable cThread;
    static DatagramSocket serverSocket;
    public static void main(String[] args) throws Exception
    {
        rTable = new RoutingTable();
        nTable = new NeighborTable();

        if (args.length == 1) {
            System.out.println("args[0]: " +args[0]);
        } else if (args.length == 2) {
            System.out.println("args[0]: " +args[0]);
            System.out.println("args[1]: " +args[1]);
        }

        Boolean usingReverse = false;
        if(args[0].equals("-reverse")) {
            //using poison reverse
            //args[1] holds router.txt
            usingReverse = true;
            System.out.println("3");
            getNeighbors(args[1]);
            System.out.println("4");
        } else {
            //not using poison reverse
            //args[0] holds router.txt info
            System.out.println("1");
            getNeighbors(args[0]);
            System.out.println("2");
        }

        try {
            serverSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        cThread = new CommandThread(rTable, serverSocket);
        lThread = new ListenerThread(port, rTable, serverSocket);
        dThread = new DVThread(rTable, port, serverSocket);

        new Thread(cThread).start();
        //cThread.run();
        new Thread(lThread).start();
        //lThread.run();
        new Thread(dThread).start();
        //dThread.run();
        System.out.println("5");
    }

    public static void getNeighbors(String filePath) {
        try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String[] lineArray;
            line = br.readLine();
            lineArray = line.split(" ");
            //lineArra{ }y[0] = this ip
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