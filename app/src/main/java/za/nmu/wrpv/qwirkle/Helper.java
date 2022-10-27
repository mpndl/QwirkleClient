package za.nmu.wrpv.qwirkle;

import static android.content.Context.VIBRATOR_SERVICE;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Helper {
    public static int BOARD_TILE_SIZE;
    public static int PLAYER_TILE_SIZE_50;
    public static int PLAYER_TILE_SIZE_60;
    public static int PLAYER_TILE_OPACITY = 128;

    static public class AnimateTilePlacement {
        private static final List<View> placementViews = new CopyOnWriteArrayList<>();
        public static  void easeInTilePlacement() {
            if (placementViews.size() > 0) {
                final int[] opacity = {PLAYER_TILE_OPACITY};
                new Thread(() -> {
                    //System.out.println("----------------------- EASE IN START ----------------------------");
                    try {
                        //System.out.println("VIEW COUNT = " + views.size());
                        for (View view : placementViews) {
                            //  System.out.println("------------------------ VIEW EASE --------------------------");
                            do {
                                view.getForeground().setAlpha(opacity[0]);
                                Thread.sleep(50);
                                //  System.out.println("EASING IN OPACITY = " + opacity[0]);
                                opacity[0] += 15;
                            } while (opacity[0] < 255);
                            opacity[0] = PLAYER_TILE_OPACITY;
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
        for (Button button: buttons) {
            if (GameModel.isTurn())
                button.setEnabled(true);
            else button.setEnabled(false);

        }
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

    public static void qwirkleAnimate(Activity context, Player player, View view) {
        Drawable gameBackground = view.getBackground();
        int playerBackgroundColor = getColor(player, context);
        ColorDrawable colorDrawable = new ColorDrawable(playerBackgroundColor);

        TextView tvQwirkle = context.findViewById(R.id.tv_qwirkle);
        int tvQwirkleSize = (int) tvQwirkle.getTextSize();
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
                        setBackgroundBorder(view, GameModel.clientPlayer, 15);
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

    public static void setTurnBackgroundBorder(View view) {
        if (GameModel.isTurn()) {
            setBackgroundBorder(view, GameModel.clientPlayer, BOARD_TILE_SIZE);
        }else view.setBackground(null);
    }

    public static void setBackgroundBorder(View view, Player player, int width) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setStroke(width, getColor(player, view.getContext()));
        view.setBackground(gradientDrawable);
    }

    public static void focusOnView(Activity context, final ScrollView scroll, final HorizontalScrollView hScroll, final View view) {
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
}
