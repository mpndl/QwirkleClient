package za.nmu.wrpv.qwirkle;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import za.nmu.wrpv.qwirkle.messages.Message;
import za.nmu.wrpv.qwirkle.messages.client.Forfeit;
import za.nmu.wrpv.qwirkle.messages.client.Stop;

public class ServerHandler implements Serializable {
    private static final BlockingQueue<Message> messages = new LinkedBlockingQueue<>();
    public static String serverAddress;

    public static ObjectOutputStream ous;
    public static ObjectInputStream ois;
    private static ServerReader serverReader;
    private static ServerWriter serverWriter;

    public static void start() {
        if (!running()) {
            serverReader = new ServerReader();
            serverReader.start();
        }
    }

    public static boolean running() {
        return serverReader != null && serverReader.isAlive();
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
            }
            finally {
                serverWriter = null;
            }
        }
    }

    public static void interrupt() {
        if (running()) {
            serverReader.interrupt();
            serverReader = null;
        }
    }

    private static class ServerReader extends Thread implements Serializable{
        @Override
        public void run() {
            try {
                Socket connection = new Socket(serverAddress, 5051);
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
            } finally {
                serverReader = null;
            }
        }
    }

    public static void send(Message message) {
        messages.add(message);
    }

    public static void stop() {
        if (serverWriter != null && serverWriter.isAlive()) {
            Forfeit message = new Forfeit();
            message.put("player", GameModel.clientPlayer);
            System.out.println("SENDING CLIENT PLAYER = " + message.get("player"));
            send(message);
        }
        else System.out.println("SERVER WRITER NOT RUNNING");
        interrupt();
    }
}
