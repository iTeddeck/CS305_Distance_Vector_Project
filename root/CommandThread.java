import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;

public class CommandThread implements Runnable {

    RoutingTable rTable;
    NeighborTable nTable;

    public CommandThread(RoutingTable rTable) {
        this.rTable = rTable;
        this.nTable = nTable;
    }

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
            if(line.contains("MSG")) {
                String[] lineArray = line.split(" ");
                // lineArray[1] = dst-ip
                // lineArray[2] = dst-port
                // lineArray[3] = msg
                //InetAddress ip = InetAddress.getByName(rTable.neighborAddresses.get(i).getIP());
                //DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, Integer.parseInt(rTable.neighborAddresses.get(i).getPort()));
                
            } else if(line.contains("PRINT")) {
                System.out.print("                ");
                for(int a = 0; a < rTable.outwardIP.size();a++) {
                    for(int i = 0; i < rTable.outwardIP.get(a).size(); i++) {
                        System.out.print(rTable.outwardIP.get(a).get(i).getIP() + "," + rTable.outwardIP.get(a).get(i).getPort() + "||");
                    }
                }
                System.out.println();

                for(int i = 0; i < rTable.costToGet.size(); i++) {
                    System.out.print(rTable.neighborAddresses.get(i).getIP() + "," + rTable.neighborAddresses.get(i).getPort() + "||");
                    for(int j = 0; j < rTable.costToGet.get(i).size(); j++) {
                        System.out.print(rTable.costToGet.get(i).get(j) + "               ");
                    }
                    System.out.println();
                }                
            } else if(line.contains("CHANGE")) {
                String[] lineArray = line.split(" ");
                
            } else {
                System.out.println("Command does not exist");
            }

            try {
                line = reader.readLine();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

}