package fr.insalyon.tc.dia.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;
import java.util.Scanner;

/**
 * Created by pvienne on 08/02/2017.
 */
public class MultiClientTest implements Runnable {

    private static int auto_id = 1;
    private final int id;

    public MultiClientTest(){
        id = auto_id;
        auto_id++;
    }

    public static void main(String... args){
        for(int i=0; i<1000; i++) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {}
            new Thread(new MultiClientTest()).start();
        }
    }

    @Override
    public void run() {
        try {
            Socket s = new Socket("localhost", 6969);
            PrintWriter writer = new PrintWriter(s.getOutputStream());
            Scanner scanner = new Scanner(s.getInputStream());
            //System.out.println("Client "+id+": Started");
            writer.println("KEYS");
            writer.flush();
            //System.out.println("Client "+id+": Setup key a");
            writer.println("PUT a 2");
            writer.println("11");
            writer.flush();
            if(getAnswer(scanner).equals("ko")){
                throw new RuntimeException("Can not put a variable");
            }
            writer.println("STOP");
            writer.flush();
            s.close();
        } catch (IOException e) {
            System.err.println("Client "+id+" has failed!");
            e.printStackTrace();
        }
    }

    private String getAnswer(Scanner scanner){
        String data = scanner.nextLine();
        while (data == null){
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {}
            data = scanner.nextLine();
        }
        return data;
    }
}
