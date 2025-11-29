package in.afi.codekosh.activity.profile;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import in.afi.codekosh.R;
import in.afi.codekosh.tools.BaseFragment;
import in.afi.codekosh.tools.ThemeBuilder;

public class ProfileEditActivity extends BaseFragment {
    private final ArrayList<String> usernames = new ArrayList<>();
    private final FirebaseDatabase _firebase = FirebaseDatabase.getInstance();
    private final DatabaseReference users = _firebase.getReference("Users");
    private String url, name_pre;
    private File avatar;
    private CircleImageView circleImageView1;
    private LinearLayout linearWord;
    private boolean aBoolean;
    private TextInputEditText nameEditText, bioEditText;
    private ImageView back, edit;
    private TextView toolbar, tx_word;
    private View divider;

    @Override
    protected boolean isHomeFragment() {
        return false;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_profile_edit;
    }

    @Override
    public void getThemeDescriptions(ThemeBuilder themeBuilder) {
        themeBuilder.setImageColorFilter(back, BLACK, WHITE);
        themeBuilder.setImageColorFilter(edit, BLACK, WHITE);

        themeBuilder.setTextColor(toolbar, BLACK, WHITE);

        if (themeBuilder.isNightMode()) {
            divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider_color_night));
        } else {
            divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider_color));
        }
    }

    @Override
    protected void initialize() {
        back = findViewById(R.id.back);
        edit = findViewById(R.id.edit);
        toolbar = findViewById(R.id.toolbar);
        tx_word = findViewById(R.id.tx_word);
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        url = intent.getStringExtra("url");
        String color = intent.getStringExtra("color");
        String bio_t = intent.getStringExtra("bio");

        avatar = null;
        aBoolean = false;
        nameEditText = findViewById(R.id.name);
        bioEditText = findViewById(R.id.bio);
        circleImageView1 = findViewById(R.id.circleimageview1);
        linearWord = findViewById(R.id.linear_word);


        if (url.equals("none")) {
            hideViews(circleImageView1);
            showViews(linearWord);
            linearWord.setBackground(new GradientDrawable() {
                public GradientDrawable getIns(int a, int b) {
                    this.setCornerRadius(a);
                    this.setColor(b);
                    return this;
                }
            }.getIns(360, Color.parseColor(color)));
            if (name != null) {
                tx_word.setText(name.substring(0, 1));
            }
        } else {
            hideViews(linearWord);
            showViews(circleImageView1);
            Glide.with(this).load(Uri.parse(url)).into(circleImageView1);
        }


        back.setOnClickListener(v -> goBack());
        edit.setOnClickListener(v -> upload());
        findViewById(R.id.picker).setOnClickListener(v -> {
            if (url.equals("none")) {
                pickSinglePhoto((profilePath, imageFileName, imageUri) -> {
                    aBoolean = true;
                    avatar = ImageUtils.compressImage(this, imageUri, 40);
                    hideViews(linearWord);
                    showViews(circleImageView1);
                    Glide.with(this).load(Uri.fromFile(avatar)).into(circleImageView1);
                });
            } else {
                createBottomSheetDialog(R.layout.sheet_profile);
                LinearLayout bt1 = (LinearLayout) bsId(R.id.bt1);
                LinearLayout bt2 = (LinearLayout) bsId(R.id.bt2);
                bt1.setOnClickListener(v1 -> pickSinglePhoto((profilePath, imageFileName, imageUri) -> {
                    aBoolean = true;
                    avatar = ImageUtils.compressImage(this, imageUri, 40);
                    hideViews(linearWord);
                    showViews(circleImageView1);
                    Glide.with(this).load(Uri.fromFile(avatar)).into(circleImageView1);
                    dismissBottomSheetDialog();
                }));
                bt2.setOnClickListener(v1 -> {
                    showProgressDialog();
                    String currentUserId = getUID();
                    DatabaseReference usersDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");

                    ValueEventListener valueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && Objects.equals(dataSnapshot.getKey(), currentUserId)) {
                                HashMap<String, Object> userData = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Object>>() {
                                });
                                if (userData != null) {
                                    String value = String.valueOf(userData.get("avatar_name"));
                                    StorageReference storageRef = FirebaseStorage.getInstance().getReference("avatar").child(value);

                                    storageRef.delete().addOnSuccessListener(unused -> {
                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("avatar", "none");
                                        hashMap.put("avatar_name", "none");
                                        pushToDatabase(hashMap, "Users", getUID(), unused1 -> {
                                            url = "none";
                                            aBoolean = false;
                                            hideViews(circleImageView1);
                                            showViews(linearWord);
                                            linearWord.setBackground(new GradientDrawable() {
                                                public GradientDrawable getIns(int a, int b) {
                                                    this.setCornerRadius(a);
                                                    this.setColor(b);
                                                    return this;
                                                }
                                            }.getIns(360, Color.parseColor(color)));
                                            if (name != null) {
                                                tx_word.setText(name.substring(0, 1));
                                            }
                                            showToast("Profile picture removed");
                                            dismissProgressDialog();
                                        }, e -> {
                                            dismissProgressDialog();
                                            alertCreator(e.getMessage());
                                        });

                                    }).addOnFailureListener(e -> alertCreator(e.getMessage()));
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle onCancelled event, if needed
                            dismissProgressDialog();
                            alertCreator("An error has occurred. Please try again later.");
                        }
                    };
                    usersDatabaseRef.child(currentUserId).addListenerForSingleValueEvent(valueEventListener);
                    dismissBottomSheetDialog();
                });
                showBottomSheetDialog();

            }
        });
        nameEditText.setText(name);
        bioEditText.setText(bio_t);
    }

    @Override
    protected void initializeLogic() {
        setUpDivider();
        back.setOnClickListener(v -> goBack());
        ChildEventListener _users_child_listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                if (_childValue != null && _childValue.containsKey("name")) {
                    usernames.add((String) _childValue.get("name"));
                }
                if (_childKey != null && _childKey.equals(getUID()) && _childValue != null) {
                    name_pre = String.valueOf(_childValue.get("name"));
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                if (_childValue != null && _childValue.containsKey("name")) {
                    usernames.add((String) _childValue.get("name"));
                }
                if (_childKey != null && _childKey.equals(getUID()) && _childValue != null) {
                    name_pre = String.valueOf(_childValue.get("name"));
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

    private void setUpDivider() {
        NestedScrollView nestedScrollView = findViewById(R.id.nested_scroll_view);
        divider = findViewById(R.id.divider);
        divider.setVisibility(View.GONE);
        nestedScrollView.setVerticalScrollBarEnabled(false);
        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> divider.setVisibility(scrollY > 0 ? View.VISIBLE : View.GONE));
    }


    private void upload() {
        closeKeyboard();
        showProgressDialog();
        String name = String.valueOf(nameEditText.getText());
        String bio = String.valueOf(bioEditText.getText());
        String currentUserId = getUID();
        DatabaseReference usersDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");
        if (TextUtils.isEmpty(name)) {
            showToast("Name empty");
            dismissProgressDialog();
        } else if (TextUtils.isEmpty(bio)) {
            showToast("Bio is empty");
            dismissProgressDialog();
        } else if (!name_pre.equals(name) && usernames.contains(name)) {
            new MaterialAlertDialogBuilder(this).setTitle("Alert").setMessage("Username already taken.").setPositiveButton("Ok", null).show();
            dismissProgressDialog();
        } else {
            if (aBoolean) {
                if (!url.equals("none")) {

                    ValueEventListener valueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && Objects.equals(dataSnapshot.getKey(), currentUserId)) {
                                HashMap<String, Object> userData = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Object>>() {
                                });
                                if (userData != null) {
                                    String value = Objects.requireNonNull(userData.get("avatar_name")).toString();
                                    StorageReference storageRef = FirebaseStorage.getInstance().getReference("avatar").child(value);

                                    // Delete the file from Firebase Storage
                                    storageRef.delete().addOnSuccessListener(unused -> push()).addOnFailureListener(e -> push());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle onCancelled event, if needed
                            alertCreator("An error has occurred. Please try again later.");
                        }
                    };
                    usersDatabaseRef.child(currentUserId).addListenerForSingleValueEvent(valueEventListener);
                } else {
                    push();
                }
            } else {
                HashMap<String, Object> dataMap = new HashMap<>();
                dataMap.put("name", String.valueOf(nameEditText.getText()));
                dataMap.put("bio", String.valueOf(bioEditText.getText()));
                String databasePath = "Users";
                String child = getUserConfig().getUid();
                pushToDatabase(dataMap, databasePath, child, unused -> {
                    dismissProgressDialog();
                    finish();
                }, e -> alertCreator(e.getMessage()));
            }
        }
    }

    private void push() {
        String s = generateFileNameWithTimestamp();
        uploadFileToFirebaseStorage("avatar", s, Uri.fromFile(avatar), uri -> {
            String fileDownloadUrl = uri.toString();
            HashMap<String, Object> dataMap = new HashMap<>();
            dataMap.put("avatar", fileDownloadUrl);
            dataMap.put("name", String.valueOf(nameEditText.getText()));
            dataMap.put("avatar_name", s);
            String databasePath = "Users";
            String child = getUserConfig().getUid();
            pushToDatabase(dataMap, databasePath, child, unused -> {
                dismissProgressDialog();
                finish();
            }, e -> alertCreator(e.getMessage()));
        }, e -> alertCreator(e.getMessage()));
    }

}