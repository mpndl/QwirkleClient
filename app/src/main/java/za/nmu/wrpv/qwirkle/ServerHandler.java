package za.nmu.wrpv.qwirkle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import za.nmu.wrpv.qwirkle.messages.Message;
import za.nmu.wrpv.qwirkle.messages.client.Stop;

public class ServerHandler implements Serializable {
    private static final BlockingQueue<Message> messages = new LinkedBlockingQueue<>();
    public static String serverAddress;
    public static String playerName;

    public static ObjectOutputStream ous;
    public static ObjectInputStream ois;
    private static ServerReader serverReader;
    private static ServerWriter serverWriter;
    @SuppressLint("StaticFieldLeak")
    public static Activity activity;

    public static void start() {
        if (!running()) {
            Log.i("game", "run: starting client");
            serverReader = new ServerReader();
            serverReader.start();
            return;
        }
        Log.i("game", "run: already running");
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
                serverWriter = null;
            }
        }
    }

    public static void interrupt() {
        if (running()) {
            Log.i("game", "interrupt: ");
            serverReader.interrupt();
            serverReader = null;
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

            } catch (ConnectException e) {
                activity.runOnUiThread(() -> {
                    Toast.makeText(activity, R.string.connection_error, Toast.LENGTH_LONG).show();
                    Button button = activity.findViewById(R.id.btn_start_game);
                    button.setEnabled(true);
                    button.setText(R.string.btn_start_game);
                });

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                serverReader = null;
                ServerHandler.stop();
            }
        }
    }

    public static void send(Message message) {
        messages.add(message);
    }

    public static void stop() {
        Log.i("game", "run: client stopped");
        Message message = new Stop();
        send(message);
    }
}
