import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ListenerThread implements Runnable {
    private int portNum;
    ServerSocket socket;
    InputStream input;
    Socket connectionSocket;

    public ListenerThread(int portNum) {
        this.portNum = portNum;

        try {
            socket = new ServerSocket(portNum);
            connectionSocket = socket.accept();
            input = connectionSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while(true) {
            byte[] bytesRecieved = null;
            try
            {
                byte[] bytes = new byte[1024];
                int numBytes = input.read(bytes);
                if ( numBytes > 0)
                {
                    bytesRecieved = new byte[numBytes];
                    System.arraycopy(bytes, 0, bytesRecieved, 0, numBytes );

                    //bytesReceived should now hold message received from other routers
                    //Need to have a way to pass perform DV adjustments based on this information
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}