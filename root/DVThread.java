import java.util.Timer;
import java.io.*;
import java.net.*;

import java.util.ArrayList;

public class DVThread implements Runnable {
    TimerSendDVTask dvTask;
    RoutingTable rTable;
    Integer portNum;
    DatagramSocket serverSocket;
    public DVThread(RoutingTable rTable, Integer portNum, DatagramSocket serverSocket, boolean poison) {
        dvTask = new TimerSendDVTask(rTable, portNum, serverSocket, poison);
        this.rTable = rTable;
        this.portNum = portNum;
        this.serverSocket = serverSocket;
    }

    public void run() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(dvTask,0,10000);
    }

}