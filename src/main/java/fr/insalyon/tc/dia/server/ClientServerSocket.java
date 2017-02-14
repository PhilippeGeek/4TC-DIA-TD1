package fr.insalyon.tc.dia.server;

import java.io.*;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Scanner;

/**
 * Create a decorator for Client socket connection.
 */
public class ClientServerSocket implements Runnable{

    private final Server server;
    private final Socket socket;
    private final BufferedReader scanner;
    private final BufferedWriter writer;

    public ClientServerSocket(Server server, Socket socket) {

        this.server = server;
        this.socket = socket;
        try {
            this.scanner = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            throw new RuntimeException("Socket is not valid!", e);
        }

    }

    public void start() {
        new Thread(this).start();
    }

    public void run() {
        while (socket.isConnected()){
            try {
                String order = scanner.readLine();
                if(order == null){
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ignored) {}
                    continue;
                }
                final String up_order = order.toUpperCase();
                if (up_order.startsWith("KEYS")) {
                    final Enumeration<String> keys = server.getKeys();
                    while (keys.hasMoreElements()) {
                        writer.write(keys.nextElement());
                        writer.newLine();
                    }
                } else if (up_order.startsWith("PUT")) {
                    try {
                        String[] data = order.split(" ", 3);
                        String key = data[1];
                        int length = Integer.parseInt(data[2]);
                        String value = "";
                        while (value.length() < length) {
                            value += scanner.readLine();
                            if (value.length() < length) {
                                value += '\n';
                            }
                        }
                        value = value.substring(0, length);
                        server.setData(key, value);
                        writer.write("OK");
                        writer.newLine();
                    } catch (IndexOutOfBoundsException | NullPointerException e) {
                        writer.write("E|PUT command has been malformed");
                        writer.newLine();
                    }
                } else if (up_order.startsWith("GET")) {
                    final String data = server.getData(order.split(" ", 2)[1]);
                    if (data != null) {
                        writer.write(data);
                        writer.newLine();
                    } else {
                        writer.write("0");
                        writer.newLine();
                    }
                } else if (up_order.startsWith("DEL")) {
                    if (server.unsetData(order.split(" ", 2)[1])) {
                        writer.write("OK");
                        writer.newLine();
                    } else {
                        writer.write("KO");
                        writer.newLine();
                    }
                } else if (up_order.startsWith("STOP")) {
                    socket.close();
                } else {
                    writer.write("E|Command not found");
                    writer.newLine();
                }
                writer.flush();
            } catch (IOException ignored){}
        }
    }
}
