package com.cometchat.pro.android.pushnotification;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.cometchat.pro.android.pushnotification.utils.MyFirebaseMessagingService;
import com.cometchat.pro.core.AppSettings;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.User;
import com.cometchat.pro.android.pushnotification.constants.AppConfig;
import com.cometchat.pro.uikit.ui_resources.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";

    private MaterialButton loginBtn;

    private MaterialCardView superhero1;

    private MaterialCardView superhero2;

    private MaterialCardView superhero3;

    private MaterialCardView superhero4;

    private AppCompatImageView ivLogo;

    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        if (CometChat.getLoggedInUser() != null)
//            startActivity(new Intent(MainActivity.this, PushNotificationActivity.class));
        AppSettings appSettings = new AppSettings.AppSettingsBuilder().subscribePresenceForAllUsers().setRegion(AppConfig.AppDetails.REGION).build();
        CometChat.init(this, AppConfig.AppDetails.APP_ID, appSettings, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String s) {
                CometChat.setSource("push-notification", "android", "java");
                Log.d(TAG, "onSuccess: " + s);
                if (CometChat.getLoggedInUser() != null)
                    startActivity(new Intent(MainActivity.this, PushNotificationActivity.class));
            }
            @Override
            public void onError(CometChatException e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        loginBtn = findViewById(R.id.login);
        superhero1 = findViewById(R.id.superhero1);
        superhero2 = findViewById(R.id.superhero2);
        superhero3 = findViewById(R.id.superhero3);
        superhero4 = findViewById(R.id.superhero4);
        ivLogo = findViewById(R.id.ivLogo);


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        superhero1.setOnClickListener(view -> {
            findViewById(R.id.superhero1_progressbar).setVisibility(View.VISIBLE);
            login("superhero1");
        });
        superhero2.setOnClickListener(view -> {
            findViewById(R.id.superhero2_progressbar).setVisibility(View.VISIBLE);
            login("superhero2");
        });
        superhero3.setOnClickListener(view -> {
            findViewById(R.id.superhero3_progressbar).setVisibility(View.VISIBLE);
            login("superhero3");
        });
        superhero4.setOnClickListener(view -> {
            findViewById(R.id.superhero4_progressbar).setVisibility(View.VISIBLE);
            login("superhero4");
        });

        if (Utils.isDarkMode(this)) {
            ivLogo.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.textColorWhite)));
        } else {
            ivLogo.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.primaryTextColor)));
        }
    }

    private void login(String uid) {
        if (CometChat.isInitialized()) {
            CometChat.login(uid, AppConfig.AppDetails.AUTH_KEY, new CometChat.CallbackListener<User>() {
                @Override
                public void onSuccess(User user) {
                    token = MyFirebaseMessagingService.token;
                    if (token == null) {
                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                    return;
                                }
                                token = task.getResult();
                                Log.e(TAG, "onComplete: " + token);
                                registerPushNotification(uid, token);
                            }
                        });
                    } else {
                        registerPushNotification(uid, token);
                    }

                }

                @Override
                public void onError(CometChatException e) {
                    String str = uid + "_progressbar";
                    int id = getResources().getIdentifier(str, "id", getPackageName());
                    findViewById(id).setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void registerPushNotification(String uid, String token) {
        Log.e(TAG, "onComplete: " + token);
        CometChat.registerTokenForPushNotification(token, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String s) {
                Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
                Log.e("onSuccessPN: ", s);
            }

            @Override
            public void onError(CometChatException e) {
                Log.e("onErrorPN: ", e.getMessage());
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        String str = uid + "_progressbar";
        int id = getResources().getIdentifier(str, "id", getPackageName());
        findViewById(id).setVisibility(View.GONE);
        startActivity(new Intent(MainActivity.this, PushNotificationActivity.class));
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
