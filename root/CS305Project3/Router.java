public class Router {
    public static void main(String[] args) throws Exception
    {
        CommandThread cThread = new CommandThread();
        RoutingTable rTable = new RoutingTable();
        DVThread dThread = new DVThread();
        
        cThread.run();
        dThread.run();
    }
}