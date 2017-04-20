import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CommandThread implements Runnable {
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = "";
        try {
            line = reader.readLine();
        } catch (Exception e) {
            System.out.println(e);
        }

        //while line is not empty
        while( line != null && !line.equals("") )
        {
            System.out.println(line);
            try {
                line = reader.readLine();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

}