package my.bestapp.multiapp.ui.reaction;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.multiapp.R;

import java.util.Random;

public class ReactionFragment extends Fragment implements View.OnTouchListener {
    Activity activity;
    View mainView;
    ConstraintLayout layout;
    TextView textHead, textSub, textHigh;
    CountDownTimer countDownTimer;
    Vibrator vibrator;

    SharedPreferences pref;

    boolean isTimerGoing = false, isWaiting = false, isClicked = false;
    boolean isVibrationEnabled, toVibrate;
    long startTime, stopTime;
    int vibrateTime;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_reaction, container, false);

        layout = mainView.findViewById(R.id.touchLayout);
        textHead = mainView.findViewById(R.id.textHead);
        textSub = mainView.findViewById(R.id.textSub);
        textHigh = mainView.findViewById(R.id.textHigh);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            vibrator =  (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        pref = activity.getSharedPreferences("Reaction_settings", Context.MODE_PRIVATE);
        isVibrationEnabled = pref.getBoolean("VibrationEnabled", true);
        vibrateTime = pref.getInt("VibrationTime", 100);

        setHighScore(-1);
        mainView.setOnTouchListener(this);
        return mainView;
    }
    private void vibrate(){
        if (vibrator!= null && isVibrationEnabled){
            // 69 is value for non-stop vibrating
            if (vibrateTime == 69){
                toVibrate = true;
                new Thread(() -> { while (toVibrate) vibrator.vibrate(50);}).start();
            }
            else vibrator.vibrate(vibrateTime);
        }
    }

    private void cancelVibrate(){
        toVibrate = false;
        if (vibrator != null)
            vibrator.cancel();
    }

    //Here we hide actionbar, status bar and navigation bar
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        activity = getActivity();
        ((AppCompatActivity)activity).getSupportActionBar().hide();

        activity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY );

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStop() {
        if (countDownTimer != null)
            countDownTimer.cancel();
        cancelVibrate();
        super.onStop();
    }

    private void setHighScore(long reactTime) {
        long highScore = pref.getLong("HighScore", 3600000);

        if (reactTime > highScore ||  reactTime == -1 && highScore != 3600000)
            textHigh.setText("Highest Score: " + highScore + "ms");

        else if (reactTime <= highScore && reactTime != -1){
            textHigh.setText("Highest score: " + reactTime + "ms");
            pref.edit().putLong("HighScore", reactTime).apply();
        }
        else textHigh.setText("Highest Score: /");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (isTimerGoing){
            layout.setBackgroundColor(Color.GREEN);
            vibrate();
            startTime = System.currentTimeMillis();
            textHead.setText(R.string.click);
            textSub.setText("");
            isWaiting = false;
            isTimerGoing = false;
            isClicked = true;
        }
        else if(isWaiting){
            isClicked = true;
            layout.setBackgroundColor(Color.RED);
            textHead.setText(R.string.to_fast);
            textSub.setText(R.string.tap_to_continue);
            isWaiting = false;
            isTimerGoing = true;
        }
        else if (isClicked){
            stopTime = System.currentTimeMillis();
            cancelVibrate();
            layout.setBackgroundResource(R.color.aqua);
            long reactTime = stopTime - startTime;
            textHead.setText(reactTime + "ms");
            setHighScore(reactTime);
            textSub.setText(R.string.tap_to_continue);
            isClicked = false;
            isWaiting = false;
            isTimerGoing = false;
        }
        else {
            layout.setBackgroundColor(Color.BLACK);
            textHigh.setText("");
            textHead.setText(R.string.wait);
            textSub.setText("");
            isWaiting = true;
            isTimerGoing = false;
            countDownTimer = new CountDownTimer(new Random().nextInt(4000) + 2000, 50) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if(isClicked){ cancel(); isClicked = false; isWaiting = false; isTimerGoing = false;}
                }

                @Override
                public void onFinish() {
                    isTimerGoing = true;
                    isWaiting = false;
                    isClicked = false;
                    onTouch(null, null);
                }
            }.start();
        }
        return false;
    }
}