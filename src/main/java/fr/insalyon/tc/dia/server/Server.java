package fr.insalyon.tc.dia.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main server class for this program.
 */
public class Server implements Runnable{

    private static final int DEFAULT_SERVER_PORT = 6969;

    private final ServerSocket socket;
    private boolean isRunning = true;
    private final ConcurrentHashMap<String, String> data = new ConcurrentHashMap<String, String>();

    private Server(int port) throws IOException {

        socket = new ServerSocket(port);

    }

    /**
     * Stop current server.
     * @return True if server has been halted
     */
    public boolean stop(){
        if(isRunning) {
            isRunning = false;
            try {
                socket.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        } else {
            return true;
        }
    }

    public static void main(String[] args){
        try {
            new Server(DEFAULT_SERVER_PORT).start();
        } catch (IOException e) {
            System.err.println("Can not start server, port is already in use.");
        }
    }

    public void start() {
        new Thread(this).start();
    }

    public void run() {
        while (isRunning){
            try {
                new ClientServerSocket(this, socket.accept()).start();
            } catch (IOException e) {
                System.err.println("Error while accepting a connection. "+e.getMessage());
            }
        }
    }

    public String getData(String key){
        return data.get(key);
    }

    public boolean setData(String key, String value){
        data.put(key, value);
        return true;
    }

    public boolean unsetData(String key){
        return null != data.remove(key);
    }

    public Enumeration<String> getKeys(){
        return data.keys();
    }

}
