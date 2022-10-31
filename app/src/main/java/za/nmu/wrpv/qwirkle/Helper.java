package za.nmu.wrpv.qwirkle;

import static android.content.Context.VIBRATOR_SERVICE;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
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

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class Helper {
    public static int BOARD_TILE_SIZE;
    public static int PLAYER_TILE_SIZE_50;
    public static int PLAYER_TILE_SIZE_60;
    public static int PLAYER_TILE_OPACITY = 128;
    public static int screenWidth;
    public  static int screenHeight;

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
            button.setEnabled(GameModel.isTurn());
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

    public static void calculatePoints(Activity context, List<Tile> visitedTiles, Player player, int qwirkleCount, GameFragment fragment) {
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

                for (Tile tile : visitedTiles) {
                    System.out.println(tile.index);
                    View view = glBoard.getChildAt(tile.index);
                    int finalPoints = points;
                    context.runOnUiThread(() -> tvPoints.setText(finalPoints + ""));
                    context.runOnUiThread(() -> {
                                focusOnView(context, sv, hsv, view);
                            });

                    points++;
                    Thread.sleep(500);
                }
                if (qwirkleCount > 0) {
                    ConstraintLayout constraintLayout = context.findViewById(R.id.cl_fragment_game);
                    context.runOnUiThread(() ->qwirkleAnimate(context, player, constraintLayout, qwirkleCount));
                    Helper.vibrate(500, context);
                }

                if (GameModel.gameEnded())
                    context.runOnUiThread(() -> fragment.gameEnded());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                context.runOnUiThread(() -> tvPoints.setVisibility(View.GONE));
            }
        }).start();
    }

    public static void displayMessage(View view, int strID) {
        Snackbar snackbar = Snackbar.make(view, view.getResources().getString(strID), Snackbar.LENGTH_INDEFINITE);
        snackbar.setTextColor(view.getContext().getColor(R.color.green));
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

    public static void setPlayerTextColor(TextView view, Player player) {
        view.setTextColor(getColor(player, view.getContext()));
    }

    public static void setTurnBackgroundBorder(View view) {
        if (GameModel.isTurn()) {
            setBackgroundBorder(view, GameModel.clientPlayer, 8);
        }else view.setBackground(null);
    }

    public static void setBackgroundBorder(View view, Player player, int width) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setStroke(width, getColor(player, view.getContext()));
        view.setBackground(gradientDrawable);
        view.getBackground().setAlpha(255);
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
