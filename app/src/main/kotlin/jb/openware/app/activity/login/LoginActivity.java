package jb.openware.app.activity.login;

import static in.afi.codekosh.tools.StringUtilsKt.privacyUrl;
import static in.afi.codekosh.tools.StringUtilsKt.termsUrl;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.transition.TransitionManager;
import android.util.Patterns;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import in.afi.codekosh.activity.home.HomeActivity;
import in.afi.codekosh.activity.splash.LinkageActivity;
import in.afi.codekosh.components.DeviceName;
import in.afi.codekosh.components.SharedPreferencesManager;
import in.afi.codekosh.tools.BaseFragment;
import in.afi.codekosh.tools.ThemeBuilder;
import jb.openware.app.R;

public class LoginActivity extends BaseFragment {
    private static final int REQUEST_CODE_ACTIVITY_B = 1;
    private final FirebaseDatabase _firebase = FirebaseDatabase.getInstance();
    private final DatabaseReference users = _firebase.getReference("Users");
    private final HashMap<String, Object> user_map = new HashMap<>();
    private final ArrayList<String> usernames = new ArrayList<>();
    private boolean login = false;
    private String token, colorCode;
    private RadialProgressView progressView;
    private TextView textview8, textview9, textview_go, textview_forgot;
    private TextInputEditText et_confirm_password, et_email, et_password, et_username;
    private TextInputLayout til_confirm_password, til_email, til_password, til_username;
    private FirebaseAuth auth;
    private HashMap<String, Object> hashMap = new HashMap<>();
    private Calendar cc = Calendar.getInstance();
    private LinearLayout refer;
    private boolean mNameEditTextVisible;
    private String id = "";
    private HashMap<String, Object> ipmap = new HashMap<>();
    private String ip_address = "";
    private String device_name = "";

    public static int getRandom(int _min, int _max) {
        return new Random().nextInt(_max - _min + 1) + _min;
    }

    public static Drawable createRoundRectDrawable(int rad, int defaultColor) {
        ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(new float[]{rad, rad, rad, rad, rad, rad, rad, rad}, null, null));
        defaultDrawable.getPaint().setColor(defaultColor);
        return defaultDrawable;
    }

    @Override
    public void onRestoreInstanceState(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        if (savedInstanceState != null) {
            mNameEditTextVisible = savedInstanceState.getBoolean("nameEditTextVisible");
        } else {
            mNameEditTextVisible = false;
        }
    }

    @Override
    protected boolean isHomeFragment() {
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ACTIVITY_B && resultCode == RESULT_OK) {
            if (login) {
                login();
            } else {
                register();
            }
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_login;
    }

    @Override
    public void getThemeDescriptions(ThemeBuilder themeBuilder) {
        themeBuilder.setTextColor(findViewById(R.id.welcome), BLACK, WHITE);
        themeBuilder.setTextColor(findViewById(R.id.w_content), GREY, TEXT_GREY);
        themeBuilder.setTextColor(findViewById(R.id.dark_not), 0xFF82868a, TEXT_GREY);
        themeBuilder.setTextColor(findViewById(R.id.textview9), 0xFF82868a, TEXT_GREY);
        themeBuilder.setTextColor(textview8, TEXT_BLACK, WHITE);
        themeBuilder.setTextColor(textview_forgot, TEXT_BLACK, WHITE);
        themeBuilder.setTextColor(findViewById(R.id.privacy), TEXT_BLACK, WHITE);
        themeBuilder.setTextColor(findViewById(R.id.terms), TEXT_BLACK, WHITE);
        themeBuilder.setTextColor(findViewById(R.id.contact), TEXT_BLACK, WHITE);
        themeBuilder.setTextColor(et_username, BLACK, WHITE);
        themeBuilder.setTextColor(et_email, BLACK, WHITE);
        themeBuilder.setTextColor(et_password, BLACK, WHITE);
        themeBuilder.setTextColor(et_confirm_password, BLACK, WHITE);

    }

    protected void initialize() {
        refer = findViewById(R.id.refer);
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                token = task.getResult();
            } else {
                alertCreator("Failed to get FCM token.", (dialog, which) -> finishAffinity());
            }
        });

        progressView = new RadialProgressView(this);
        progressView.setProgressColor(Color.WHITE);
        progressView.setSize(dp());
        FrameLayout linear_go = findViewById(R.id.linear_go);
        linear_go.setBackground(createRoundRectDrawable(27, Color.parseColor("#006493")));
        linear_go.addView(progressView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 17));
        removeScrollBar(findViewById(R.id.scroll1));

        auth = FirebaseAuth.getInstance();
        til_email = findViewById(R.id.email_layout);
        til_username = findViewById(R.id.username_layout);
        til_password = findViewById(R.id.password_layout);
        til_confirm_password = findViewById(R.id.confirm_password_layout);
        et_email = findViewById(R.id.email);
        et_username = findViewById(R.id.username);
        et_password = findViewById(R.id.password);
        et_confirm_password = findViewById(R.id.confirm_password);
        textview8 = findViewById(R.id.textview8);
        textview9 = findViewById(R.id.textview9);
        textview_forgot = findViewById(R.id.forgot);
        TextView textview_privacy = findViewById(R.id.privacy);
        TextView textview_terms = findViewById(R.id.terms);
        TextView textview_contact = findViewById(R.id.contact);
        textview_go = findViewById(R.id.textview_go);

        et_email.setTextColor(Color.BLACK);
        et_password.setTextColor(Color.BLACK);
        et_username.setTextColor(Color.BLACK);
        et_confirm_password.setTextColor(Color.BLACK);

        toggle(!mNameEditTextVisible);
        setLoading(false);

        textview_privacy.setOnClickListener(v -> openUrl(privacyUrl));
        textview_terms.setOnClickListener(v -> openUrl(termsUrl));

        linear_go.setOnClickListener(view1 -> {
            CodeTool.hideKeyboard(refer);
            if ((login && !validateLogin()) || (!login && !validateRegister())) {
                return;
            }
            new MaterialAlertDialogBuilder(this).setTitle("Disclaimer").setMessage("By proceeding, you are certifying that you have perused and consented to our terms and conditions. \nFurther information can be found on our terms and conditions page.").setNegativeButton("Cancel", null).setPositiveButton("Continue", (dialog, which) -> {
                Intent intent = new Intent(this, CaptchaActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ACTIVITY_B);
            }).show();
        });

        textview8.setOnClickListener(v -> {
            toggle(!login);
            mNameEditTextVisible = !login;
        });

        textview_forgot.setOnClickListener(v -> startActivity(ResetActivity.class));

        textview_contact.setOnClickListener(view14 -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:codekosh.afi@gmail.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "CodeKosh");
            try {
                startActivity(Intent.createChooser(intent, "Choose email provider"));
            } catch (Exception ignored) {
                // Handle exception if needed
            }
        });
    }

    protected void initializeLogic() {
        components();
        ChildEventListener _users_child_listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                if (_childValue != null && _childValue.containsKey("name")) {
                    usernames.add((String) _childValue.get("name"));
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                if (_childValue != null && _childValue.containsKey("name")) {
                    usernames.add((String) _childValue.get("name"));
                }

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot _param1, String _param2) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot _param1) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError _param1) {

            }
        };
        users.addChildEventListener(_users_child_listener);
    }

    @SuppressLint("HardwareIds")
    private void components() {
        getConnectionsManager("https://api.ipify.org/?format=json", GET, new RequestListener() {
            @Override
            public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {
                try {
                    ipmap = new Gson().fromJson(response, new TypeToken<HashMap<String, Object>>() {
                    }.getType());
                    ip_address = String.valueOf(ipmap.get("ip"));
                } catch (Exception e) {
                    // Handle JSON parsing exception
                }
            }

            @Override
            public void onErrorResponse(String tag, String message) {

            }
        });

        id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        DeviceName.init(this);
        try {
            device_name = DeviceName.getDeviceName();
        } catch (Exception e) {
            device_name = e.toString();
        }
    }

    private int dp() {
        return (int) (getResources().getDisplayMetrics().density * (float) 25.0);
    }

    private void setLoading(boolean bl) {
        TransitionManager.beginDelayedTransition(refer);
        if (bl) {
            textview_go.setVisibility(View.GONE);
            progressView.setVisibility(View.VISIBLE);
            return;
        }
        textview_go.setVisibility(View.VISIBLE);
        progressView.setVisibility(View.GONE);
    }

    @SuppressLint("SetTextI18n")
    private void toggle(boolean bl) {
        if (bl) {
            til_username.setVisibility(View.GONE);
            til_confirm_password.setVisibility(View.GONE);
            til_email.setVisibility(View.VISIBLE);
            til_password.setVisibility(View.VISIBLE);
            textview8.setText("Create account");
            textview9.setText("New user?");
            textview_go.setText("Login");
        } else {
            til_password.setVisibility(View.VISIBLE);
            til_email.setVisibility(View.VISIBLE);
            til_confirm_password.setVisibility(View.VISIBLE);
            til_username.setVisibility(View.VISIBLE);
            textview8.setText("Login");
            textview9.setText("Already have an account?");
            textview_go.setText("Register");
        }
        login = bl;
    }

    private boolean validateLogin() {
        til_email.setError(null);
        til_password.setError(null);
        if (!Patterns.EMAIL_ADDRESS.matcher(String.valueOf(et_email.getText())).matches()) {
            til_email.setError("Email is invalid");
            return false;
        }
        if (String.valueOf(et_password.getText()).length() < 8) {
            til_password.setError("Password must be at least 8 characters");
            return false;
        }
        return true;
    }

    private boolean validateRegister() {
        til_username.setError(null);
        til_email.setError(null);
        til_password.setError(null);
        til_confirm_password.setError(null);
        if (!String.valueOf(et_username.getText()).trim().matches("[a-zA-Z0-9_\\-.]*")) {
            til_username.setError("Username contains invalid characters");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(String.valueOf(et_email.getText())).matches()) {
            til_email.setError("Email is invalid");
            return false;
        }
        if (String.valueOf(et_password.getText()).length() < 8) {
            til_password.setError("Password must be at least 8 characters");
            return false;
        }
        if (!String.valueOf(et_password.getText()).equals(String.valueOf(et_confirm_password.getText()))) {
            til_confirm_password.setError("Passwords must match");
            return false;
        }
        return true;
    }

    public void _pickRandomColor() {
        double n = getRandom(0, 7);
        if (n == 0) {
            colorCode = "#65A9E0";
        }
        if (n == 1) {
            colorCode = "#E56555";
        }
        if (n == 2) {
            colorCode = "#5FBED5";
        }
        if (n == 3) {
            colorCode = "#F2739A";
        }
        if (n == 4) {
            colorCode = "#76C84C";
        }
        if (n == 5) {
            colorCode = "#8D84EE";
        }
        if (n == 6) {
            colorCode = "#50A6E6";
        }
        if (n == 7) {
            colorCode = "#F28C48";
        }
        if (n == 8) {
            colorCode = "#009688";
        }
        if (n == 9) {
            colorCode = "#00FE5E";
        }
        if (n == 10) {
            colorCode = "#000000";
        }
        if (n == 11) {
            colorCode = "#795548";
        }
        if (n == 12) {
            colorCode = "#795548";
        }
        if (n == 13) {
            colorCode = "#E64A19";
        }
        if (n == 14) {
            colorCode = "#FFC107";
        }
        if (n == 15) {
            colorCode = "#E91E63";
        }
        if (n == 16) {
            colorCode = "#00BCD4";
        }
    }

    public void removeScrollBar(final View view) {
        view.setVerticalScrollBarEnabled(false);
        view.setHorizontalScrollBarEnabled(false);
    }

    private void login() {
        setLoading(true);

        String emailTxt = stringFormat(et_email.getText()).trim();
        String passwordTxt = stringFormat(et_password.getText());

        auth.signInWithEmailAndPassword(emailTxt, passwordTxt).addOnCompleteListener(task -> {
            setLoading(false);

            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();

                if (user != null) {
                    if (user.isEmailVerified()) {
                        user_map.put("device_name", device_name);
                        user_map.put("device_id", id);
                        user_map.put("ip_address", ip_address);
                        users.child(user.getUid()).updateChildren(user_map);
                        user_map.clear();
                        getUserConfig().saveLoginDetails(emailTxt, passwordTxt, user.getUid());
                        Intent intent = new Intent();
                        if (Objects.equals(new SharedPreferencesManager(getParentActivity()).getString("link", "null"), "null")) {
                            intent.setClass(this, HomeActivity.class);
                        } else {
                            intent.setClass(this, LinkageActivity.class);
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        new MaterialAlertDialogBuilder(this).setTitle("Notice").setMessage("To proceed, account verification is necessary. Please check your email for the verification process.").setPositiveButton("RESEND", (dialog, which) -> user.sendEmailVerification().addOnCompleteListener(task12 -> {
                            if (task12.isSuccessful()) {
                                new MaterialAlertDialogBuilder(this).setTitle("Alert").setMessage("A verification link has been dispatched to your email.").setPositiveButton("OK", null).show();
                            } else {
                                alertCreator(Objects.requireNonNull(task12.getException()).getMessage());
                            }
                        })).show();
                    }
                }
            } else {
                String errorMessage = Objects.requireNonNull(task.getException()).getMessage();

                if (errorMessage != null) {
                    if (errorMessage.contains("invalid or the user does not have a password")) {
                        alertCreator("Authentication failed due to an incorrect password entry or a recent password modification.");
                    } else if (errorMessage.contains("There is no user record corresponding to this identifier")) {
                        alertCreator("No user exists with the provided email address on our servers, possibly due to deletion of the account.");
                    } else if (errorMessage.contains("The email address is already in use by another account")) {
                        alertCreator("The account is already registered on our servers. Please use a different email or sign in using the registered email.");
                    } else if (errorMessage.contains("The user account has been disabled by an administrator")) {
                        alertCreator("The user account has been banned by the administrator for violating our terms and conditions.");
                    } else if (errorMessage.contains("The email address is badly formatted")) {
                        alertCreator("We regret to inform you that the email address provided is not in a proper format.");
                    } else {
                        alertCreator(errorMessage);
                    }
                }
            }
        });
    }

    private void register() {
        setLoading(true);

        String emailTxt = Objects.requireNonNull(et_email.getText()).toString().trim();
        String passwordTxt = Objects.requireNonNull(et_password.getText()).toString();
        String usernameTxt = Objects.requireNonNull(et_username.getText()).toString();
        if (usernames.contains(usernameTxt)) {
            alertCreator("Username already taken.");
        } else {
            auth.createUserWithEmailAndPassword(emailTxt, passwordTxt).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();

                    if (user != null) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(usernameTxt).build();

                        String uid = user.getUid();
                        _pickRandomColor();
                        cc = Calendar.getInstance();
                        hashMap = new HashMap<>();
                        hashMap.put("name", usernameTxt);
                        hashMap.put("email", emailTxt);
                        hashMap.put("uid", uid);
                        hashMap.put("bio", "Developer");
                        hashMap.put("likes", "0");
                        hashMap.put("downloads", "0");
                        hashMap.put("projects", "0");
                        hashMap.put("color", colorCode);
                        hashMap.put("device_id", id);
                        hashMap.put("device_name", device_name);
                        hashMap.put("ip_address", ip_address);
                        hashMap.put("block", "false");
                        hashMap.put("registration_date", String.valueOf(cc.getTimeInMillis()));
                        hashMap.put("verified", "false");
                        hashMap.put("password", passwordTxt);
                        hashMap.put("token", token);
                        hashMap.put("notify", "true");
                        hashMap.put("avatar", "none");
                        hashMap.put("badge", "0");
                        users.child(uid).updateChildren(hashMap);
                        hashMap.clear();

                        user.updateProfile(profileUpdates).addOnCompleteListener(task12 -> {
                            if (task12.isSuccessful()) {
                                user.sendEmailVerification().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        setLoading(false);
                                        toggle(true);
                                        new MaterialAlertDialogBuilder(this).setTitle("Message").setMessage("A verification link has been dispatched to your email address.\n*Check spam folder also.").setPositiveButton("OK", null).show();
                                        FirebaseAuth.getInstance().signOut();
                                    } else {
                                        setLoading(false);
                                        alertCreator(Objects.requireNonNull(task1.getException()).getMessage());
                                    }
                                });
                            } else {
                                setLoading(false);
                                alertCreator(Objects.requireNonNull(task12.getException()).getMessage());
                            }
                        });
                    }
                } else {
                    setLoading(false);
                    alertCreator(Objects.requireNonNull(task.getException()).getMessage());
                }
            });
        }
    }

}