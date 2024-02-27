import java.io.IOException;
import java.net.DatagramSocket;


// class that creates a thread when the client starts
public class Server {
    public static void main(String[] args) throws IOException {
        while(true) {
            DatagramSocket s = new DatagramSocket();
            HandleClient c = new HandleClient(s);
            // start a new thread
            Thread t = new Thread(c);
            t.start();
        }
    }
}
