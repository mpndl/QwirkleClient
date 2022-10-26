package za.nmu.wrpv.qwirkle;

import static android.content.Context.VIBRATOR_SERVICE;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import java.util.List;

public class Helper {
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

    public static void setTurnBackgroundBorder(View view) {
        if (GameModel.isTurn()) {
            setBackgroundBorder(view, GameModel.clientPlayer, GameFragment.BOARD_TILE_SIZE / 6);
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

    public static <V extends View>  void easeInTilePlacement(List<V> views) {
        final int[] opacity = {GameFragment.PLAYER_TILE_OPACITY};
        new Thread(() -> {
            //System.out.println("----------------------- EASE IN START ----------------------------");
            try {
                //System.out.println("VIEW COUNT = " + views.size());
                for (View view : views) {
                    //  System.out.println("------------------------ VIEW EASE --------------------------");
                    do {
                        view.getForeground().setAlpha(opacity[0]);
                        Thread.sleep(50);
                        //  System.out.println("EASING IN OPACITY = " + opacity[0]);
                        opacity[0]+= 15;
                    } while (opacity[0] < 255);
                    opacity[0] = GameFragment.PLAYER_TILE_OPACITY;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                views.clear();
                // System.out.println("----------------------------- EASE IN END ----------------------");
            }
        }).start();
    }
}
