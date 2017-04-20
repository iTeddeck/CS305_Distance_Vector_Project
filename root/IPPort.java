public class IPPort {
    String ip;
    String port;
    
    public IPPort(String ip, String port) {
        this.ip = ip;
        this.port = port;
    }
    
    public void setIP(String newIP) {
        this.ip = newIP;
    }
    
    public void setPort(String newPort) {
        this.port = newPort;
    }
    
    public String getIP() {
        return this.ip;
    }
    
    public String getPort() {
        return this.port;
    }
    
}