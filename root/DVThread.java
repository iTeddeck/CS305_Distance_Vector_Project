import java.util.Timer;
import java.io.*;
import java.net.*;

public class DVThread implements Runnable {
    TimerSendDVTask dvTask;
    RoutingTable rTable;
    Integer portNum;
    DatagramSocket serverSocket;
    public DVThread(RoutingTable rTable, Integer portNum, DatagramSocket serverSocket) {
        dvTask = new TimerSendDVTask(rTable, portNum, serverSocket);
        this.rTable = rTable;
        this.portNum = portNum;
        this.serverSocket = serverSocket;
    }

    public void run() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(dvTask,1000,5000);
    }

}