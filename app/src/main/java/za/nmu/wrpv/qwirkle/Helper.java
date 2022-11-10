package za.nmu.wrpv.qwirkle;

import static android.content.Context.VIBRATOR_SERVICE;
import static android.content.Context.WIFI_SERVICE;

import static za.nmu.wrpv.qwirkle.ServerHandler.connectErrCount;
import static za.nmu.wrpv.qwirkle.ServerHandler.connectTimeout;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import za.nmu.wrpv.qwirkle.messages.client.ConnectionError;
import za.nmu.wrpv.qwirkle.messages.client.Stop;

public class Helper {
    public static int BOARD_TILE_SIZE;
    public static int PLAYER_TILE_SIZE_50;
    public static int PLAYER_TILE_SIZE_60;
    public static int PLAYER_TILE_OPACITY = 128;
    public static int screenWidth;
    public static int screenHeight;

    public static void connectError() {
        if (connectErrCount < connectTimeout) {
            new Stop().apply();
            connectErrCount++;
        } else {
            ConnectionError message = new ConnectionError();
            message.put("connectErrCount", connectErrCount);
            message.put("gameID", GameModel.gameID);
            ServerHandler.send(message);
            new Stop().apply();
        }
    }

    public static void turnErrCheck(Activity context) {
        Button btnPlay = context.findViewById(R.id.btn_play);
        if ((!GameModel.isTurn() && btnPlay.isEnabled()) || (GameModel.isTurn() && !btnPlay.isEnabled())) Helper.connectError();
    }

    public static void initializeTileSizes(Activity context) {
        Display display = context.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x; // int screenWidth = display.getWidth(); on API < 13
        screenHeight = size.y; // int screenHeight = display.getHeight(); on API <13

        BOARD_TILE_SIZE = screenWidth/6;
        PLAYER_TILE_SIZE_60 = screenWidth/8;
        PLAYER_TILE_SIZE_50 = screenWidth/9;
    }

    static public class AnimateTilePlacement {
        private static final List<View> placementViews = new CopyOnWriteArrayList<>();
        public static  void easeInTilePlacement(int milliseconds) {
            if (placementViews.size() > 0) {
                final int[] opacity = {PLAYER_TILE_OPACITY};
                new Thread(() -> {
                    //System.out.println("----------------------- EASE IN START ----------------------------");
                    try {
                        //System.out.println("VIEW COUNT = " + views.size());
                        for (View view : placementViews) {
                            //  System.out.println("------------------------ VIEW EASE --------------------------");
                            if (view != null) {
                                do {
                                    view.getForeground().setAlpha(opacity[0]);
                                    Thread.sleep(milliseconds);
                                    //  System.out.println("EASING IN OPACITY = " + opacity[0]);
                                    opacity[0] += 15;
                                } while (opacity[0] < 255);
                                opacity[0] = PLAYER_TILE_OPACITY;
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        placementViews.clear();
                        // System.out.println("----------------------------- EASE IN END ----------------------");
                    }
                }).start();
            }
        }

        public static void add(View view) {
            placementViews.add(view);
        }
    }

    public static int getColor(Player player, Context context) {
        switch (player.color) {
            case "red":
                return context.getColor(R.color.red);
            case "green":
                return context.getColor(R.color.green);
            case "blue":
                return context.getColor(R.color.blue);
            case "purple":
                return context.getColor(R.color.purple);
        }
        return 0;
    }

    public static void enableIfTurn(Button... buttons) {
        try {
            for (Button button : buttons) {
                button.setEnabled(GameModel.isTurn());
            }
        }catch (NullPointerException ignored) {}
    }

    public static void setTurnBackgroundColor(View view, Player player) {
        if (GameModel.isTurn()) view.setBackgroundColor(getColor(player, view.getContext()));
        else view.setBackground(null);
    }

    public static void setBackgroundColor(View view, Player player) {
        view.setBackgroundColor(getColor(player, view.getContext()));
    }

    public static Drawable getDrawable(String name, Activity context) {
        return context.getDrawable(context.getResources().getIdentifier(name, "drawable", context.getPackageName()));
    }

    public static void vibrate(int milliseconds, Activity context) {
        Vibrator v = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(milliseconds);
        }
    }

    public static void restart(Activity context) {
        Intent mStartActivity = new Intent(context, BeginActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    public static void animateCalculatePoints(Activity context, List<Tile> visitedTiles, Player player, int qwirkleCount, GameFragment fragment) {
        System.out.println("------------------------- CALCULATING POINTS -> size = " + visitedTiles.size());
        System.out.println(player.name + " -> points = " + player.points);
        GridLayout glBoard = context.findViewById(R.id.board);
        TextView tvPoints = context.findViewById(R.id.tv_points);
        context.runOnUiThread(() -> tvPoints.setVisibility(View.VISIBLE));
        ScrollView sv = context.findViewById(R.id.scrollView2);
        HorizontalScrollView hsv = context.findViewById(R.id.horizontalScrollView);
        setPlayerTextColor(tvPoints, player);
        new Thread(() -> {
            try {
                int points = 1;

                List<View> views = new ArrayList<>();
                for (Tile tile : visitedTiles) {
                    View view = glBoard.getChildAt(tile.index);
                    fragment.focusView = view;
                    views.add(view);
                    int finalPoints = points;
                    context.runOnUiThread(() -> tvPoints.setText(finalPoints + ""));
                    context.runOnUiThread(() -> {
                        try {
                            focusOnView(context, sv, hsv, view);
                            view.getForeground().setAlpha(PLAYER_TILE_OPACITY);
                        }catch (NullPointerException ignored){}
                            });

                    points++;
                    Thread.sleep(500);
                }

                Collections.reverse(visitedTiles);
                Collections.reverse(views);
                AnimateTilePlacement.placementViews.addAll(views);

                AnimateTilePlacement.easeInTilePlacement(20);
                for (Tile tile: visitedTiles) {
                    View view = glBoard.getChildAt(tile.index);
                    context.runOnUiThread(() -> focusOnView(context, sv, hsv, view));
                    Thread.sleep(30);
                }

                if (qwirkleCount > 0) {
                    ConstraintLayout constraintLayout = context.findViewById(R.id.cl_fragment_game);
                    context.runOnUiThread(() ->qwirkleAnimate(context, player, constraintLayout, qwirkleCount));
                    Helper.vibrate(500, context);
                }

                if (GameModel.gameEnded()) context.runOnUiThread(fragment::gameEnded);
            } catch (InterruptedException | NullPointerException e) {
                e.printStackTrace();
            }
            finally {
                context.runOnUiThread(() -> tvPoints.setVisibility(View.GONE));
            }
        }).start();
    }

    public static void displayMessage(View view, int strID, int color) {
        Snackbar snackbar = Snackbar.make(view, view.getResources().getString(strID), Snackbar.LENGTH_INDEFINITE);
        snackbar.setTextColor(view.getContext().getColor(color));
        snackbar.setAction(view.getResources().getString(R.string.ok), view1 -> {
            snackbar.dismiss();
        });
        snackbar.setBackgroundTint(Color.BLACK);

        View snackbarView = snackbar.getView();
        TextView snackbarTextView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        snackbarTextView.setMaxLines(3);

        snackbar.show();
    }

    public static void qwirkleAnimate(Activity context, Player player, View view, int qwirkleCount) {
        Drawable gameBackground = view.getBackground();
        int playerBackgroundColor = getColor(player, context);
        ColorDrawable colorDrawable = new ColorDrawable(playerBackgroundColor);

        TextView tvQwirkle = context.findViewById(R.id.tv_qwirkle);
        int tvQwirkleSize = (int) tvQwirkle.getTextSize();
        if (qwirkleCount > 1) {
            tvQwirkle.setText(tvQwirkle.getText() + " X" + qwirkleCount);
        }
        new Thread(() -> {
            sound(context, R.raw.qwirkle);
            try {
                context.runOnUiThread(() -> {
                    view.setBackground(colorDrawable);
                    tvQwirkle.setVisibility(View.VISIBLE);
                    tvQwirkle.setTextColor(playerBackgroundColor);
                });
                for (int i = 255; i > 128; i-= 15) {
                    int finalI = i;
                    context.runOnUiThread(() -> colorDrawable.setAlpha(finalI));
                    Thread.sleep(50);
                }

                for (int i = tvQwirkleSize; i < tvQwirkleSize + (BOARD_TILE_SIZE / 3); i+=15) {
                    int finalI = i;
                    context.runOnUiThread(() -> tvQwirkle.setTextSize(finalI));
                    Thread.sleep(50);
                }

                for (int i = 128; i < 255; i+= 15) {
                    int finalI = i;
                    context.runOnUiThread(() -> colorDrawable.setAlpha(finalI));
                    Thread.sleep(50);
                }

                for (int i = tvQwirkleSize + (BOARD_TILE_SIZE / 3); i > tvQwirkleSize; i-=15) {
                    int finalI = i;
                    context.runOnUiThread(() -> tvQwirkle.setTextSize(finalI));
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                context.runOnUiThread(() -> {
                    view.setBackground(gameBackground);
                    tvQwirkle.setVisibility(View.GONE);
                    if (GameModel.isTurn()) {
                        setBackgroundBorder(view, GameModel.player, 15);
                    }else view.setBackground(gameBackground);
                });
            }
        }).start();
    }

    public synchronized static void sound(Activity context, int resid) {
        MediaPlayer player = MediaPlayer.create(context, resid);
        player.setOnCompletionListener(MediaPlayer::release);
        player.start();
    }

    public static void setPlayerTextColor(TextView view, Player player) {
        view.setTextColor(getColor(player, view.getContext()));
    }

    public static void setTurnBackgroundBorder(View view) {
        if (GameModel.isTurn()) {
            setBackgroundBorder(view, GameModel.player, 12);
        }else view.setBackground(null);
    }

    private static String getMobileIPAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {}
        return "";
    }

    public static String getWifiIPAddress(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        return Formatter.formatIpAddress(ip);
    }

    private static List<String> getIpAddresses() {
        List<String> ipAddresses = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        //ipAddresses.add(inetAddress.getHostAddress());
                        String ip = inetAddress.getHostAddress();
                        String[] i = Objects.requireNonNull(ip).split("\\.");
                        if (i.length > 2) ipAddresses.add(ip);
                    }
                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return ipAddresses;
    }

    public static List<String> getIPRange(Context context) {
        List<String> range = new ArrayList<>();
        String wifi = getWifiIPAddress(context);
        String ip = "";
        if (!wifi.equals("0.0.0.0")) {
            String[] split = wifi.split("\\.");
            StringBuilder newIP = new StringBuilder();
            for(int j = 0; j < split.length - 1; j++) {
                newIP.append(split[j]).append(".");
            }
            ip = newIP.toString();
        }
        else {
            List<String> ips = getIpAddresses();
            String i = ips.get(0);
            String[] split = i.split("\\.");
            StringBuilder newIP = new StringBuilder();
            for(int j = 0; j < split.length - 1; j++) {
                newIP.append(split[j]).append(".");
            }
            ip = newIP.toString();
        }
        for (int i = 0; i <= 255; i++) {
            range.add(ip + i);
        }
        return range;
    }

    public static void setBackgroundBorder(View view, Player player, int width) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setStroke(width, getColor(player, view.getContext()));
        view.setBackground(gradientDrawable);
        view.getBackground().setAlpha(255);
    }

    public static void focusOnView(Activity context, final ScrollView scroll, final HorizontalScrollView hScroll, final View view) {
        if (view == null) return;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;

        int scrollTo = ((View) view.getParent().getParent()).getTop() + view.getTop() - (height/2);
        scroll.smoothScrollTo(0, scrollTo);

        hsvFocusOnView(context, hScroll, view);
    }

    private static void hsvFocusOnView(Activity context, final HorizontalScrollView scroll, final View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        int scrollTo = ((View)view.getParent()).getLeft() + view.getLeft() - (width/2);
        scroll.scrollTo(scrollTo,0);
    }

    public synchronized static boolean isOnline() {
        try {
            int timeoutMs = 1500;
            Socket sock = new Socket();
            SocketAddress sockaddr = new InetSocketAddress("8.8.8.8", 53);

            sock.connect(sockaddr, timeoutMs);
            sock.close();

            return true;
        } catch (IOException e) { return false; }
    }
}
