package za.nmu.wrpv.qwirkle;

import android.app.Activity;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import za.nmu.wrpv.qwirkle.messages.Message;
import za.nmu.wrpv.qwirkle.messages.client.Forfeit;
import za.nmu.wrpv.qwirkle.messages.client.Stop;

public class ServerHandler implements Serializable {
    private static final BlockingQueue<Message> messages = new LinkedBlockingQueue<>();
    public static String serverAddress;
    public static List<String> serverAddresses;
    public static int clientID = -1;

    public static ObjectOutputStream ous;
    public static ObjectInputStream ois;
    private static ServerReader serverReader;
    private static ServerWriter serverWriter;

    public static int connectErrCount = 0;
    public static final int connectTimeout = 6;

    public static void start() {
        if (!running()) {
            System.out.println("========================== ESTABLISHING CONNECTION");
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
            } finally {
                serverWriter = null;
            }
        }
    }

    public static void interrupt() {
        if (running()) serverReader.interrupt();
        serverReader = null;
    }

    private static Socket getConnection() throws ConnectException {
        for (String ip: serverAddresses) {
            System.out.println("ATTEMPTING IP = " + ip);
            try {
                return new Socket(ip, 5051);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        throw new ConnectException("COULD NOT CONNECT USING THE IP ADDRESSES");
    }

    private static class ServerReader extends Thread implements Serializable{
        @Override
        public void run() {
            boolean conn = false;
            try {
                Socket connection;
                if (serverAddress == null) connection = getConnection();
                else connection = new Socket(serverAddress, 5051);

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

            }catch (ConnectException e) {
                conn = true;
            }
            catch (ClassCastException e) {
                BeginActivity.runLater(d -> Helper.restart((Activity) d.get("context")));
            }
            catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                System.out.println("----------------------------- SERVER READER STOPPED");
                serverReader = null;
                if (!conn) new Stop().apply();
            }
        }
    }

    public static void send(Message message) {
        if (!running()) start();
        messages.add(message);
    }

    public static void stop() {
        if (running()) {
            Forfeit message = new Forfeit();
            message.put("player", GameModel.player);
            send(message);
        }
        interrupt();
    }
}
