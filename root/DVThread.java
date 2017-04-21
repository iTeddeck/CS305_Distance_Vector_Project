import java.util.Timer;
public class DVThread implements Runnable {
    TimerSendDVTask dvTask;
    RoutingTable rTable;
    Integer portNum;
    
    public DVThread(RoutingTable rTable, Integer portNum) {
        dvTask = new TimerSendDVTask(rTable, portNum);
        this.rTable = rTable;
        this.portNum = portNum;
    }
    
    public void run() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(dvTask,0,1000);
    }
}