import java.util.Timer;
public class DVThread implements Runnable {
    TimerSendDVTask dvTask = new TimerSendDVTask();
    public void run() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(dvTask,5000,1000);
    }
}