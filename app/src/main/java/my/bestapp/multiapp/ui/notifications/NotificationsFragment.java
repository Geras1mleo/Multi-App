package my.bestapp.multiapp.ui.notifications;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.multiapp.R;

public class NotificationsFragment extends Fragment {

    View mainView;
    Activity activity;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_notifications, container, false);

        activity = getActivity();
        setNormalUIFlags();
        return mainView;
    }
    private void setNormalUIFlags() {

        ((AppCompatActivity)activity).getSupportActionBar().show();
        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }
}