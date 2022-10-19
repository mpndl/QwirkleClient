package za.nmu.wrpv.qwirkle;

import android.annotation.SuppressLint;
import android.app.Activity;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import za.nmu.wrpv.qwirkle.messages.Message;
import za.nmu.wrpv.qwirkle.messages.client.Stop;
import za.nmu.wrpv.qwirkle.messages.client.Subscribe;

public class ServerHandler implements Serializable {
    private static final BlockingQueue<Message> messages = new LinkedBlockingQueue<>();
    private static final String serverAddress = "10.0.245.165";

    public static ObjectOutputStream ous;
    public static ObjectInputStream ois;
    private static ServerReader serverReader;
    private static ServerWriter serverWriter;
    @SuppressLint("StaticFieldLeak")
    public static Activity activity;

    public static void start() {
        if (!running()) {
            serverReader = new ServerReader();
            serverReader.start();
        }
    }

    public static boolean running() {
        return serverReader != null;
    }

    private static class ServerWriter extends Thread implements Serializable {
        @Override
        public void run() {
            try {
                do {
                    Message msg = messages.take();
                    ous.writeObject(msg);
                    ous.flush();
                }while (true);
            }catch (InterruptedException | IOException e) {
                e.printStackTrace();
                serverWriter = null;
            }
        }
    }

    private static class ServerReader extends Thread implements Serializable{
        @Override
        public void run() {
            try {
                Socket connection = new Socket(serverAddress, 5050);
                ois = new ObjectInputStream(connection.getInputStream());
                ous = new ObjectOutputStream(connection.getOutputStream());
                ous.flush();

                serverWriter = new ServerWriter();
                serverWriter.start();

                Message msg;
                do {
                    msg = (Message) ois.readObject();
                    msg.apply();
                }while (true);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }finally {
                serverReader = null;
                ServerHandler.stop();
            }
        }
    }

    public static void send(Message message) {
        messages.add(message);
    }

    public static void stop() {
        Message message = new Stop();
        send(message);
    }
}
