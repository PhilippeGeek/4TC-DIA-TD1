package fr.insalyon.tc.dia.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Scanner;

/**
 * Create a decorator for Client socket connection.
 */
public class ClientServerSocket implements Runnable{

    private final Server server;
    private final Socket socket;
    private final Scanner scanner;
    private final PrintWriter writer;

    public ClientServerSocket(Server server, Socket socket) {

        this.server = server;
        this.socket = socket;
        try {
            this.scanner = new Scanner(socket.getInputStream());
            this.writer = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException("Socket is not valid!", e);
        }

    }

    public void start() {
        new Thread(this).start();
    }

    public void run() {
        while (socket.isConnected()){
            String order = scanner.nextLine();
            final String up_order = order.toUpperCase();
            if(up_order.startsWith("KEYS")){
                final Enumeration<String> keys = server.getKeys();
                while(keys.hasMoreElements()){
                    writer.println(keys.nextElement());
                }
            } else if(up_order.startsWith("PUT")){
                try{
                    String[] data = order.split(" ", 3);
                    String key = data[1];
                    int length = Integer.parseInt(data[2]);
                    String value = "";
                    while (value.length() < length) {
                        value += scanner.nextLine();
                        if(value.length() < length){
                            value += '\n';
                        }
                    }
                    value = value.substring(0, length);
                    server.setData(key, value);
                    writer.println("ok");
                } catch (IndexOutOfBoundsException|NullPointerException e){
                    writer.println("E|PUT command has been malformed");
                }
            } else if(up_order.startsWith("GET")){
                final String data = server.getData(order.split(" ", 2)[1]);
                if(data != null)
                    writer.println(data);
                else
                    writer.println('0');
            } else if(up_order.startsWith("DEL")){
                if(server.unsetData(order.split(" ", 2)[1]))
                    writer.println("ok");
                else
                    writer.println("ko");
            } else {
                writer.println("E|Command was not found!");
            }
            writer.flush();
        }
    }
}
