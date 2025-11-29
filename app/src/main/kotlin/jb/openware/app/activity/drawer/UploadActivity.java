package in.afi.codekosh.activity.drawer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import in.afi.codekosh.R;
import in.afi.codekosh.components.FileUtil;
import in.afi.codekosh.components.FirebaseUtils;
import in.afi.codekosh.components.SketchwareUtils;
import in.afi.codekosh.tools.BaseFragment;
import in.afi.codekosh.tools.ThemeBuilder;

public class UploadActivity extends BaseFragment {
    // ArrayLists
    private final ArrayList<String> screen = new ArrayList<>();
    private final ArrayList<HashMap<String, Object>> data_screenshots2 = new ArrayList<>();
    // Firebase References
    private final StorageReference avatar = FirebaseStorage.getInstance().getReference("screenshots");
    private final DatabaseReference project = FirebaseDatabase.getInstance().getReference("projects/normal");
    private final DatabaseReference premium_server = FirebaseDatabase.getInstance().getReference("projects/premium");
    private ArrayList<HashMap<String, Object>> data_screenshots;
    private ArrayList<HashMap<String, Object>> temp_listmap1 = new ArrayList<>();
    // Strings
    private String icon_url = "";
    private String icon_path = "";
    private String file_url = "";
    private String getID = "";

    // TextInputEditText and Layouts
    private TextInputEditText title_msg, description_msg, whats_new_msg, premium_string;
    private TextInputLayout t1, t2, t4;

    // ImageViews
    private ImageView imageView;
    private CircleImageView circle_img;

    // RecyclerView
    private RecyclerView recycler_screenshots;

    // TextViews
    private TextView project_type, version_msg, file_msg, category_text;

    // Booleans and Doubles
    private boolean new_project, retro;
    private double n = 1;
    private double j = 0;

    // UI Components
    private MaterialSwitch premium, comments, visibility;
    private ImageView back;
    private View divider;

    // Other Variables
    private HashMap<String, Object> receivedHashMap;
    private OnCompleteListener<Uri> _image_shared_upload_success_listener;


    @Override
    protected boolean isHomeFragment() {
        return false;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_upload;
    }

    @Override
    public void getThemeDescriptions(@NonNull ThemeBuilder themeBuilder) {
        themeBuilder.setTextColor(findViewById(R.id.title), BLACK, WHITE);
        themeBuilder.setImageColorFilter(back, BLACK, WHITE);
        themeBuilder.setImageColorFilter(findViewById(R.id.done), BLACK, WHITE);
        themeBuilder.setTextColor(title_msg, TEXT_BLACK, TEXT_WHITE);
        themeBuilder.setTextColor(description_msg, TEXT_BLACK, TEXT_WHITE);
        themeBuilder.setTextColor(whats_new_msg, TEXT_BLACK, TEXT_WHITE);
        themeBuilder.setTextColor(premium_string, TEXT_BLACK, TEXT_WHITE);
        themeBuilder.setTextColor(findViewById(R.id.icon_text), TEXT_BLACK, WHITE);


        themeBuilder.setTextColor(category_text, TEXT_BLACK, WHITE);
        themeBuilder.setTextColor(project_type, TEXT_BLACK, WHITE);
        themeBuilder.setTextColor(file_msg, TEXT_BLACK, WHITE);
        themeBuilder.setTextColor(version_msg, TEXT_BLACK, WHITE);
        themeBuilder.setTextColor(version_msg, TEXT_BLACK, TEXT_WHITE);

        themeBuilder.setTextColor(findViewById(R.id.m1), BLACK, WHITE);
        themeBuilder.setTextColor(findViewById(R.id.m2), BLACK, WHITE);
        themeBuilder.setTextColor(findViewById(R.id.m3), BLACK, WHITE);

        themeBuilder.setTextColor(visibility, BLACK, WHITE);
        themeBuilder.setTextColor(comments, BLACK, WHITE);
        themeBuilder.setTextColor(premium, BLACK, WHITE);

        if (themeBuilder.isNightMode()) {
            divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider_color_night));
        } else {
            divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider_color));
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initialize() {
        back = findViewById(R.id.back);
        imageView = findViewById(R.id.circle);
        circle_img = findViewById(R.id.circle_img);
        premium = findViewById(R.id.premium);
        comments = findViewById(R.id.comments);
        visibility = findViewById(R.id.visibility);
        file_msg = findViewById(R.id.file_msg);
        project_type = findViewById(R.id.project_type);
        version_msg = findViewById(R.id.version_msg);
        MaterialCardView materialCardView3 = findViewById(R.id.screenshot);
        recycler_screenshots = new RecyclerView(getParentActivity());
        data_screenshots = new ArrayList<>();
        LinearLayout category = findViewById(R.id.category);
        category_text = findViewById(R.id.category_text);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getParentActivity(), LinearLayoutManager.VERTICAL, false);
        recycler_screenshots.setLayoutManager(layoutManager);
        materialCardView3.addView(recycler_screenshots);
        title_msg = findViewById(R.id.title_msg);
        description_msg = findViewById(R.id.description_msg);
        whats_new_msg = findViewById(R.id.whats_new_msg);
        premium_string = findViewById(R.id.premium_string);

        t1 = findViewById(R.id.t1);
        t2 = findViewById(R.id.t2);
        t4 = findViewById(R.id.t4);
        Intent intent = getIntent();

        TextView data = findViewById(R.id.data);
        receivedHashMap = (HashMap<String, Object>) intent.getSerializableExtra("hashmap");
        if (receivedHashMap == null) {
            hideViews(findViewById(R.id.warning), findViewById(R.id.whats_new_msg));
            new_project = true;
            hideViews(t4);
            comments.setChecked(true);
            visibility.setChecked(true);
        } else {
            String dateString = add(String.valueOf(receivedHashMap.get("time")));
            String messageTemplate = "You have recently updated your project, your project will not update to the top of the projects list until %s. Updating your project will reset the timer back to 3 days.";
            String formattedMessage = String.format(messageTemplate, dateString);
            data.setText(formattedMessage);
            showViews(findViewById(R.id.whats_new_msg));
            new_project = false;
            icon_path = String.valueOf(receivedHashMap.get("icon"));
            Glide.with(UploadActivity.this).load(Uri.parse(String.valueOf(receivedHashMap.get("icon")))).centerCrop().into(circle_img);
            showViews(circle_img);
            hideViews(imageView, findViewById(R.id.warning));
            title_msg.setText(String.valueOf(receivedHashMap.get("title")));
            description_msg.setText(String.valueOf(receivedHashMap.get("description")));
            if (receivedHashMap.containsKey("whats_new")) {
                if (!String.valueOf(receivedHashMap.get("whats_new")).equals("none")) {
                    whats_new_msg.setText(String.valueOf(receivedHashMap.get("whats_new")));
                }
            }
            category_text.setText(String.valueOf(receivedHashMap.get("category")));
            visibility.setChecked(String.valueOf(receivedHashMap.get("visibility")).equals("true"));
            comments.setChecked(String.valueOf(receivedHashMap.get("comments_visibility")).equals("true"));
            if (receivedHashMap.containsKey("unlock_code")) {
                if (!String.valueOf(receivedHashMap.get("unlock_code")).equals("none")) {
                    premium.setChecked(true);
                    premium_string.setText(String.valueOf(receivedHashMap.get("unlock_code")));
                    showViews(t4);
                } else {
                    hideViews(t4);
                }
            }
            {
                HashMap<String, Object> _item = new HashMap<>();
                _item.put("path", "empty");
                _item.put("name", "Add photos");
                data_screenshots.add(_item);
                ArrayList<String> temp = new Gson().fromJson(String.valueOf(receivedHashMap.get("screenshots")), new TypeToken<ArrayList<String>>() {
                }.getType());
                int screenshotNumber = 1;
                for (String code : temp) {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("name", "Screenshot" + screenshotNumber);
                    hashMap.put("path", code);
                    screenshotNumber++;
                    data_screenshots.add(hashMap);
                }
            }

        }

        delayTask(() -> category.setOnClickListener(v -> {
            String[] themes = {"Books & Reference", "Business & Trading", "Communication", "Education", "Entertainment", "Example & Tutorial", "Games", "Multi-Device", "Music & Audio", "Other", "Photographic", "Productivity", "Social", "Tools", "UI & UX", "Videography"};

            String selectedCategory = String.valueOf(category_text.getText());
            int[] checkedItem = {0};

            for (int i = 0; i < themes.length; i++) {
                if (themes[i].equals(selectedCategory)) {
                    checkedItem[0] = i;
                    break;
                }
            }

            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(getParentActivity(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered);
            dialogBuilder.setTitle("Select a category");
            dialogBuilder.setIcon(R.drawable.category);
            dialogBuilder.setSingleChoiceItems(themes, checkedItem[0], (dialog, which) -> checkedItem[0] = which);
            dialogBuilder.setPositiveButton("Apply", (dialog, which) -> {
                String selectedTheme = themes[checkedItem[0]];
                for (String theme : themes) {
                    if (theme.equals(selectedTheme)) {
                        category_text.setText(theme);
                        break;
                    }
                }
            });
            dialogBuilder.setNegativeButton("Cancel", null);
            dialogBuilder.show();


        }));

        _image_shared_upload_success_listener = param -> {
            final String _downloadUrl = param.getResult().toString();
            screen.add(_downloadUrl);
            if (new_project) {
                if (n < data_screenshots.size() - 1) {
                    n++;
                    String profileName = Uri.parse(String.valueOf(data_screenshots.get((int) n).get("path"))).getLastPathSegment();
                    StorageReference fileReference;
                    if (profileName != null) {
                        fileReference = avatar.child(profileName);
                        File avatar = ImageUtils.compressImage(this, Uri.fromFile(new File(String.valueOf(data_screenshots.get((int) n).get("path")))), 40);
                        fileReference.putFile(Uri.fromFile(avatar)).addOnFailureListener(e -> new MaterialAlertDialogBuilder(this).setTitle("Error").setMessage(e.getMessage()).setPositiveButton("Retry", null).show()).continueWithTask(task -> fileReference.getDownloadUrl()).addOnCompleteListener(_image_shared_upload_success_listener);
                    }
                } else {
                    String j;
                    if (String.valueOf(project_type.getText()).equals("Sketchware Pro(Mod)")) {
                        j = generateProjectNameWithTimestamp() + ".swb";
                    } else {
                        j = generateProjectNameWithTimestamp() + ".zip";
                    }
                    uploadFileToFirebaseStorage("projects", j, Uri.fromFile(new File(String.valueOf(file_msg.getText()))), uri -> {
                        file_url = uri.toString();
                        upload();
                    }, e -> alertCreator(e.getMessage()));
                }
            } else {
                if (j < data_screenshots2.size() - 1) {
                    j++;
                    String profileName = Uri.parse(String.valueOf(data_screenshots2.get((int) j).get("path"))).getLastPathSegment();
                    StorageReference fileReference = avatar.child(profileName);
                    File avatar = ImageUtils.compressImage(this, Uri.fromFile(new File(String.valueOf(data_screenshots2.get((int) j).get("path")))), 40);
                    fileReference.putFile(Uri.fromFile(avatar)).addOnFailureListener(e -> new MaterialAlertDialogBuilder(this).setTitle("Error").setMessage(e.getMessage()).setPositiveButton("Retry", null).show()).continueWithTask(task -> fileReference.getDownloadUrl()).addOnCompleteListener(_image_shared_upload_success_listener);

                } else {
                    if (retro) {
                        upload();
                    } else {
                        String j;
                        if (String.valueOf(project_type.getText()).equals("Sketchware Pro(Mod)")) {
                            j = generateProjectNameWithTimestamp() + ".swb";
                        } else {
                            j = generateProjectNameWithTimestamp() + ".zip";
                        }
                        uploadFileToFirebaseStorage("projects", j, Uri.fromFile(new File(String.valueOf(file_msg.getText()))), uri -> {
                            file_url = uri.toString();
                            upload();
                        }, e -> alertCreator(e.getMessage()));
                    }
                }
            }
        };


    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void initializeLogic() {
        setUpDivider();
        retrieveProjectsListFromFirebase(value -> getID = value);
        back.setOnClickListener(v -> goBack());
        premium.setOnCheckedChangeListener((buttonView, isChecked) -> t4.setVisibility(isChecked ? View.VISIBLE : View.GONE));
        if (new_project) {
            showViews(imageView);
            hideViews(circle_img);
            {
                HashMap<String, Object> _item = new HashMap<>();
                _item.put("path", "empty");
                _item.put("name", "Add photos");
                data_screenshots.add(_item);
            }
        }
        hideViews(findViewById(R.id.file), findViewById(R.id.version));
        recycler_screenshots.setAdapter(new Memories_listAdapter(data_screenshots));

        findViewById(R.id.pick_image).setOnClickListener(v -> pickSinglePhoto((profilePath, imageFileName, imageUri) -> {
            File avatar = ImageUtils.compressImage(this, imageUri, 40);
            Glide.with(UploadActivity.this).load(Uri.fromFile(avatar)).centerCrop().into(circle_img);
            icon_path = String.valueOf(Uri.fromFile(avatar));
            showViews(circle_img);
            hideViews(imageView);
        }));
        findViewById(R.id.type).setOnClickListener(v -> {
            String[] themes = {"Android Studio", "Sketchware", "Sketchware Pro(Mod)", "HTML"};
            int[] checkedItem = {0};

            String selectedProjectType = String.valueOf(project_type.getText());
            for (int i = 0; i < themes.length; i++) {
                if (themes[i].equals(selectedProjectType)) {
                    checkedItem[0] = i;
                    break;
                }
            }
            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered);
            dialogBuilder.setIcon(R.drawable.category);
            dialogBuilder.setTitle("Select Project Type");
            dialogBuilder.setSingleChoiceItems(themes, checkedItem[0], (dialog, which) -> checkedItem[0] = which);
            dialogBuilder.setPositiveButton("Apply", (dialog, which) -> {
                String selectedTheme = themes[checkedItem[0]];
                switch (selectedTheme) {
                    case "Android Studio":
                        project_type.setText("Android Studio");
                        file_msg.setText("Select a file");
                        showViewWithAnimation(findViewById(R.id.file));
                        hideViewWithAnimation(findViewById(R.id.version));
                        break;
                    case "Sketchware":
                        project_type.setText("Sketchware");
                        file_msg.setText("Select a file");
                        showViewWithAnimation(findViewById(R.id.file));
                        hideViewWithAnimation(findViewById(R.id.version));
                        break;
                    case "Sketchware Pro(Mod)":
                        project_type.setText("Sketchware Pro(Mod)");
                        file_msg.setText("Select a file");
                        showViewWithAnimation(findViewById(R.id.file));
                        showViewWithAnimation(findViewById(R.id.version));
                        break;
                    case "HTML":
                        project_type.setText("HTML");
                        file_msg.setText("Select a file");
                        showViewWithAnimation(findViewById(R.id.file));
                        hideViewWithAnimation(findViewById(R.id.version));
                        break;
                }
            });
            dialogBuilder.setNegativeButton("Cancel", null);
            dialogBuilder.show();
        });
        findViewById(R.id.version).setOnClickListener(v -> {
            String[] themes = {"Sketchware Pro v6.4.0 test build 05", "Sketchware Pro v6.4.0 test build 04", "Sketchware Pro v6.4.0 test build 03", "Sketchware Pro v6.4.0 test build 02", "Sketchware Pro v6.4.0 test build 01", "Sketchware Pro v6.4.0 Beta 6"};

            int[] checkedItem = {0};

            String currentTheme = String.valueOf(version_msg.getText());

            for (int i = 0; i < themes.length; i++) {
                if (themes[i].equals(currentTheme)) {
                    checkedItem[0] = i;
                    break;
                }
            }

            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered);
            dialogBuilder.setIcon(R.drawable.code);
            dialogBuilder.setTitle("Sketchware Pro version");
            dialogBuilder.setSingleChoiceItems(themes, checkedItem[0], (dialog, which) -> checkedItem[0] = which);
            dialogBuilder.setPositiveButton("Apply", (dialog, which) -> {
                String selectedTheme = themes[checkedItem[0]];
                for (String theme : themes) {
                    if (theme.equals(selectedTheme)) {
                        version_msg.setText(theme);
                        break;
                    }
                }
            });
            dialogBuilder.setNegativeButton("Cancel", null);
            dialogBuilder.show();
        });
        findViewById(R.id.file).setOnClickListener(v -> {
            if (String.valueOf(project_type.getText()).equals("Sketchware Pro(Mod)")) {
                pickFile(swb, (filePath, fileName) -> {
                    if (getFileExtension(filePath).equals(".swb")) {
                        file_msg.setText(filePath);
                    } else {
                        showToast("Please select '.swb' file");
                    }
                });
            } else if (String.valueOf(project_type.getText()).equals("Sketchware")) {
                if (!checkPermission()) {
                    alertCreator("Storage permission is required in order to load and upload the sketchware project.", (dialog, which) -> checkStorage(new PermissionListener() {
                        @Override
                        public void onGranted() {
                            syncTask(new SyncTaskListener() {
                                @Override
                                public void beforeTaskStart() {
                                    showLoadingDialog("Loading Projects");
                                }

                                @Override
                                public void onBackground() {
                                    temp_listmap1.clear();
                                    temp_listmap1 = new SketchwareUtils().loadProjects();
                                }

                                @Override
                                public void onTaskComplete() {
                                    dismissLoadingDialog();
                                    showProjectDialog();
                                }
                            });
                        }

                        @Override
                        public void onNotGranted() {
                            alertCreator("Storage permission is required in order to load and upload the sketchware project.");
                        }
                    }));
                } else {
                    syncTask(new SyncTaskListener() {
                        @Override
                        public void beforeTaskStart() {
                            showLoadingDialog("Loading Projects");
                        }

                        @Override
                        public void onBackground() {
                            temp_listmap1.clear();
                            temp_listmap1 = new SketchwareUtils().loadProjects();
                        }

                        @Override
                        public void onTaskComplete() {
                            dismissLoadingDialog();
                            showProjectDialog();
                        }
                    });
                }
            } else {
                pickFile(zip, (filePath, fileName) -> file_msg.setText(filePath));
            }
        });
        findViewById(R.id.done).setOnClickListener(v -> {
            hideKeyboard(findViewById(R.id.nested_scroll_view));
            if (checkData()) {
                String size = FileUtil.getFileSize(String.valueOf(file_msg.getText()));
                if (FileSizeChecker.isFileSizeLessThan25MB(size)) {
                    MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered);
                    dialogBuilder.setNegativeButton("Cancel", null);
                    if (!new_project) {
                        if (String.valueOf(file_msg.getText()).equals("Select a file")) {
                            retro = true;
                            dialogBuilder.setIcon(R.drawable.info);
                            dialogBuilder.setTitle("Warning");
                            dialogBuilder.setMessage("Your project file hasn't been modified. It will still be uploaded, but it won't be bumped to the top of the list. Do you want to proceed?");
                            dialogBuilder.setPositiveButton("Proceed", (dialog, which) -> upload_update());
                        } else {
                            retro = false;
                            if (String.valueOf(project_type.getText()).equals("Select project type")) {
                                alertToast("Select project type");
                                return;
                            } else if (String.valueOf(project_type.getText()).equals("Sketchware Pro(Mod)")) {
                                if (String.valueOf(version_msg.getText()).equals("Sketchware Pro version")) {
                                    alertToast("Select Sketchware pro version");
                                    return;
                                } else {
                                    dialogBuilder.setIcon(R.drawable.upload);
                                    dialogBuilder.setTitle("Upload Project");
                                    dialogBuilder.setMessage("By uploading your project, you agree to our terms and policies. Please make sure you have read and understood our user policies before proceeding.");
                                    dialogBuilder.setPositiveButton("Upload", (dialog, which) -> upload_update());
                                }

                            } else {
                                dialogBuilder.setIcon(R.drawable.upload);
                                dialogBuilder.setTitle("Upload Project");
                                dialogBuilder.setMessage("By uploading your project, you agree to our terms and policies. Please make sure you have read and understood our user policies before proceeding.");
                                dialogBuilder.setPositiveButton("Upload", (dialog, which) -> upload_update());
                            }
                        }
                    } else {
                        dialogBuilder.setIcon(R.drawable.upload);
                        dialogBuilder.setTitle("Upload Project");
                        dialogBuilder.setMessage("By uploading your project, you agree to our terms and policies. Please make sure you have read and understood our user policies before proceeding.");
                        dialogBuilder.setPositiveButton("Upload", (dialog, which) -> {
                            showProgressDialog();
                            String s = generateFileNameWithTimestamp();
                            uploadFileToFirebaseStorage("icon", s, Uri.parse(icon_path), uri -> {
                                icon_url = uri.toString();
                                n = 1;
                                String profileName = Uri.parse(String.valueOf(data_screenshots.get(1).get("path"))).getLastPathSegment();
                                StorageReference fileReference = avatar.child(profileName);
                                File avatar = ImageUtils.compressImage(this, Uri.fromFile(new File(String.valueOf(data_screenshots.get((int) n).get("path")))), 40);

                                fileReference.putFile(Uri.fromFile(avatar)).addOnFailureListener(e -> new MaterialAlertDialogBuilder(this).setTitle("Error").setMessage(e.getMessage()).setPositiveButton("Retry", null).show()).continueWithTask(task -> fileReference.getDownloadUrl()).addOnCompleteListener(_image_shared_upload_success_listener);

                            }, e -> alertCreator(e.getMessage()));
                        });
                    }
                    dialogBuilder.show();
                } else {
                    alertCreator("Your project file size should not exceed 25MB.");
                }
            }

        });

    }

    private void upload_update() {
        for (int i = 1; i < data_screenshots.size(); i++) {
            HashMap<String, Object> screenshot = data_screenshots.get(i);
            if (screenshot.containsKey("path")) {
                if (String.valueOf(screenshot.get("path")).contains("https")) {
                    String path = (String) screenshot.get("path");
                    screen.add(path);
                }
            }
        }
        if (icon_path.contains("https")) {
            showProgressDialog();
            icon_url = icon_path;
            if (!data_screenshots2.isEmpty()) {
                j = 0;
                if (String.valueOf(data_screenshots2.get(0).get("path")).contains("https")) {
                    screen.add(String.valueOf(data_screenshots2.get((int) j).get("path")));
                } else {
                    String profileName = Uri.parse(String.valueOf(data_screenshots2.get((int) j).get("path"))).getLastPathSegment();
                    StorageReference fileReference = avatar.child(profileName);
                    File avatar = ImageUtils.compressImage(this, Uri.fromFile(new File(String.valueOf(data_screenshots2.get((int) j).get("path")))), 40);
                    fileReference.putFile(Uri.fromFile(avatar)).addOnFailureListener(e -> new MaterialAlertDialogBuilder(this).setTitle("Error").setMessage(e.getMessage()).setPositiveButton("Retry", null).show()).continueWithTask(task -> fileReference.getDownloadUrl()).addOnCompleteListener(_image_shared_upload_success_listener);
                }
            } else {
                if (retro) {
                    upload();
                } else {
                    String j;
                    if (String.valueOf(project_type.getText()).equals("Sketchware Pro(Mod)")) {
                        j = generateProjectNameWithTimestamp() + ".swb";
                    } else {
                        j = generateProjectNameWithTimestamp() + ".zip";
                    }
                    uploadFileToFirebaseStorage("projects", j, Uri.fromFile(new File(String.valueOf(file_msg.getText()))), uri -> {
                        file_url = uri.toString();
                        upload();
                    }, e -> alertCreator(e.getMessage()));
                }
            }


        } else {
            showProgressDialog();
            String s = generateFileNameWithTimestamp();
            uploadFileToFirebaseStorage("icon", s, Uri.parse(icon_path), uri -> {
                icon_url = uri.toString();
                j = 0;
                String profileName = Uri.parse(String.valueOf(data_screenshots2.get(0).get("path"))).getLastPathSegment();
                StorageReference fileReference = avatar.child(profileName);
                File avatar = ImageUtils.compressImage(this, Uri.fromFile(new File(String.valueOf(data_screenshots2.get((int) j).get("path")))), 40);

                fileReference.putFile(Uri.fromFile(avatar)).addOnFailureListener(e -> new MaterialAlertDialogBuilder(this).setTitle("Error").setMessage(e.getMessage()).setPositiveButton("Retry", null).show()).continueWithTask(task -> fileReference.getDownloadUrl()).addOnCompleteListener(_image_shared_upload_success_listener);

            }, e -> alertCreator(e.getMessage()));
        }
    }

    @NonNull
    private Boolean checkData() {
        if (new_project) {
            if (icon_path.equals("")) {
                alertToast("Please select icon");
                return false;
            } else if (TextUtils.isEmpty(title_msg.getText())) {
                alertToast("Title must not be empty");
                return false;
            } else if (String.valueOf(title_msg.getText()).length() > 50) {
                t1.setError("Field must not exceed max length");
                return false;
            } else if (TextUtils.isEmpty(description_msg.getText())) {
                alertToast("Description must not be empty");
                return false;
            } else if (String.valueOf(description_msg.getText()).length() > 1500) {
                t2.setError("Field must not exceed max length");
                return false;
            } else if (data_screenshots.size() < 3) {
                alertToast("Select at-least 2 screenshots");
                return false;
            } else if (String.valueOf(project_type.getText()).equals("Select project type")) {
                alertToast("Select project type");
                return false;
            } else if (String.valueOf(category_text.getText()).equals("Select a category")) {
                alertToast("Select category of the project");
                return false;
            } else if (String.valueOf(file_msg.getText()).equals("Select a file")) {
                alertToast("Select project file");
                return false;
            } else if (premium.isChecked()) {
                if (String.valueOf(premium_string.getText()).equals("")) {
                    alertToast("Enter the unlock code");
                    return false;
                } else {
                    return true;
                }
            } else if (String.valueOf(project_type.getText()).equals("Sketchware Pro(Mod)")) {
                if (String.valueOf(version_msg.getText()).equals("Sketchware Pro version")) {
                    alertToast("Select Sketchware pro version");
                    return false;
                } else {
                    return true;
                }

            } else {
                return true;
            }
        } else {
            if (icon_path.equals("")) {
                alertToast("Please select icon");
                return false;
            } else if (TextUtils.isEmpty(title_msg.getText())) {
                alertToast("Title must not be empty");
                return false;
            } else if (String.valueOf(title_msg.getText()).length() > 50) {
                t1.setError("Field must not exceed max length");
                return false;
            } else if (TextUtils.isEmpty(description_msg.getText())) {
                alertToast("Description must not be empty");
                return false;
            } else if (String.valueOf(description_msg.getText()).length() > 1500) {
                t2.setError("Field must not exceed max length");
                return false;
            } else if (TextUtils.isEmpty(whats_new_msg.getText())) {
                alertToast("What's new message must not be empty");
                return false;
            } else if (data_screenshots.size() < 3) {
                alertToast("Select at-least 2 screenshots");
                return false;
            } else if (String.valueOf(category_text.getText()).equals("Select a category")) {
                alertToast("Select category of the project");
                return false;
            } else {
                return true;
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private void upload() {
        syncTask(new SyncTaskListener() {
            @Override
            public void beforeTaskStart() {

            }

            @Override
            public void onBackground() {
                Calendar c = Calendar.getInstance();
                String normal_key = project.push().getKey();
                if (normal_key != null) {
                    if (new_project) {
                        HashMap<String, Object> data_map = new HashMap<>();

                        String projectType = String.valueOf(project_type.getText());
                        boolean isSketchwarePro = projectType.equals("Sketchware Pro(Mod)");

                        data_map.put("icon", icon_url);
                        data_map.put("title", String.valueOf(title_msg.getText()).trim());
                        data_map.put("download_url", file_url);
                        data_map.put("size", FileUtil.getFileSize(String.valueOf(file_msg.getText())));
                        data_map.put("verify", "false");
                        data_map.put("uid", getUID());
                        data_map.put("key", normal_key);
                        data_map.put("likes", "0");
                        data_map.put("project_type", projectType);
                        data_map.put("comments", "0");
                        data_map.put("downloads", "0");
                        data_map.put("whats_new", "none");
                        data_map.put("category", category_text.getText().toString());
                        data_map.put("editors_choice", "false");
                        data_map.put("trending", "false");
                        data_map.put("latest", "true");
                        data_map.put("name", getUserConfig().getName());
                        data_map.put("sketchware_pro_version", isSketchwarePro ? String.valueOf(version_msg.getText()) : "none");
                        data_map.put("description", String.valueOf(description_msg.getText()).trim());
                        int currentPoints = Integer.parseInt(getID);
                        int updatedPoints = currentPoints + 1;
                        data_map.put("id", String.valueOf(updatedPoints));
                        Gson gson = new Gson();
                        data_map.put("screenshots", gson.toJson(screen));

                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, h:mm a");
                        data_map.put("time", dateFormat.format(c.getTime()));
                        data_map.put("update_time", "none");
                        data_map.put("comments_visibility", comments.isChecked() ? "true" : "false");
                        data_map.put("visibility", visibility.isChecked() ? "true" : "false");

                        if (premium.isChecked()) {
                            data_map.put("unlock_code", String.valueOf(premium_string.getText()));
                            premium_server.child(normal_key).updateChildren(data_map);
                            updateProjectsListToFirebase(unused -> {

                            });
                        } else {
                            data_map.put("unlock_code", "none");
                            project.child(normal_key).updateChildren(data_map);
                            updateProjectsListToFirebase(unused -> {

                            });
                        }

                    } else {
                        String update_key = (String) receivedHashMap.get("key");
                        HashMap<String, Object> data_map = new HashMap<>();

                        String projectType = String.valueOf(project_type.getText());
                        if (retro) {
                            data_map.put("download_url", receivedHashMap.get("download_url"));
                            data_map.put("size", receivedHashMap.get("size"));
                            data_map.put("project_type", receivedHashMap.get("project_type"));
                            boolean isSketchwarePro = String.valueOf(receivedHashMap.get("project_type")).equals("Sketchware Pro(Mod)");
                            data_map.put("sketchware_pro_version", isSketchwarePro ? receivedHashMap.get("sketchware_pro_version") : "none");
                        } else {
                            boolean isSketchwarePro = projectType.equals("Sketchware Pro(Mod)");
                            data_map.put("project_type", projectType);
                            data_map.put("download_url", file_url);
                            data_map.put("size", FileUtil.getFileSize(String.valueOf(file_msg.getText())));
                            data_map.put("sketchware_pro_version", isSketchwarePro ? String.valueOf(version_msg.getText()) : "none");
                        }
                        data_map.put("icon", icon_url);
                        data_map.put("title", String.valueOf(title_msg.getText()).trim());
                        data_map.put("verify", "false");
                        data_map.put("uid", getUID());
                        data_map.put("key", update_key);
                        data_map.put("likes", receivedHashMap.get("likes"));
                        data_map.put("comments", receivedHashMap.get("comments"));
                        data_map.put("downloads", receivedHashMap.get("downloads"));
                        data_map.put("id", receivedHashMap.get("id"));
                        data_map.put("whats_new", String.valueOf(whats_new_msg.getText()));
                        data_map.put("category", category_text.getText().toString());
                        data_map.put("editors_choice", "false");
                        data_map.put("trending", "false");
                        data_map.put("latest", "true");
                        data_map.put("name", receivedHashMap.get("name"));
                        data_map.put("description", String.valueOf(description_msg.getText()).trim());
                        Gson gson = new Gson();
                        data_map.put("screenshots", gson.toJson(screen));
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, h:mm a");
                        data_map.put("time", receivedHashMap.get("time"));
                        data_map.put("update_time", dateFormat.format(c.getTime()));
                        data_map.put("comments_visibility", comments.isChecked() ? "true" : "false");
                        data_map.put("visibility", visibility.isChecked() ? "true" : "false");

                        if (premium.isChecked()) {
                            data_map.put("unlock_code", String.valueOf(premium_string.getText()));
                            if (update_key != null) {
                                if (retro) {
                                    premium_server.child(update_key).updateChildren(data_map);
                                } else {
                                    premium_server.child(update_key).removeValue().addOnSuccessListener(unused -> premium_server.child(update_key).updateChildren(data_map));
                                }
                            }
                        } else {
                            data_map.put("unlock_code", "none");
                            if (update_key != null) {
                                if (retro) {
                                    project.child(update_key).updateChildren(data_map);
                                } else {
                                    project.child(update_key).removeValue().addOnSuccessListener(unused -> project.child(update_key).updateChildren(data_map));
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onTaskComplete() {
                dismissProgressDialog();
                new FirebaseUtils().increaseUserKeyData("projects", getUID());
                new MaterialAlertDialogBuilder(UploadActivity.this).setTitle("Success").setMessage("Project upload successfully.").setPositiveButton("Ok", (dialog, which) -> goBack()).show();
            }
        });


    }

    private void setUpDivider() {
        NestedScrollView nestedScrollView = findViewById(R.id.nested_scroll_view);
        divider = findViewById(R.id.divider);
        divider.setVisibility(View.GONE);
        nestedScrollView.setVerticalScrollBarEnabled(false);
        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> divider.setVisibility(scrollY > 0 ? View.VISIBLE : View.GONE));
    }

    private void showViewWithAnimation(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            return;
        }

        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate().alpha(1f).setDuration(300) // Duration of the animation in milliseconds
                .setListener(null);
    }

    private void hideViewWithAnimation(View view) {
        if (view.getVisibility() == View.GONE) {
            return;
        }

        view.animate().alpha(0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
            }
        });
    }


    private String add(String inputDateString) {
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("MMMM d, h:mm a", Locale.ENGLISH);
        Date inputDate = null;
        try {
            inputDate = inputDateFormat.parse(inputDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (inputDate != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(inputDate);
            calendar.add(Calendar.DAY_OF_MONTH, 3);
            Date newDate = calendar.getTime();
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMMM d, h:mm a", Locale.ENGLISH);
            return outputDateFormat.format(newDate);
        }
        return " ";
    }

    @SuppressLint("SetTextI18n")
    public void showProjectDialog() {
        final MaterialAlertDialogBuilder p_dialog = new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered);
        p_dialog.setTitle("Select a project");
        p_dialog.setIcon(R.drawable.ic_file);
        View inflate = getLayoutInflater().inflate(R.layout.dialog_cus, null);
        p_dialog.setView(inflate);
        final AlertDialog dialog = p_dialog.create();

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) LinearLayout linear1 = inflate.findViewById(R.id.linear1);


        final ListView listView2 = new ListView(this);
        listView2.setDivider(null);
        listView2.setDividerHeight(0);
        listView2.setLayoutParams(new GridView.LayoutParams(GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.WRAP_CONTENT));
        listView2.setAdapter(new List1Adapter(temp_listmap1));
        ((BaseAdapter) listView2.getAdapter()).notifyDataSetChanged();
        linear1.addView(listView2);


        listView2.setOnItemClickListener((parent, view, _pos, id) -> {
            String s = "temp";
            if (temp_listmap1.get(_pos).containsKey("my_app_name")) {
                s = String.valueOf(temp_listmap1.get(_pos).get("my_app_name"));
            }
            String finalS = s;
            syncTask(new SyncTaskListener() {
                @Override
                public void beforeTaskStart() {
                    showLoadingDialog("Initializing Project");
                }

                @Override
                public void onBackground() {
                    new SketchwareUtils().exportProject(finalS, _pos, temp_listmap1);
                }

                @Override
                public void onTaskComplete() {
                    dismissLoadingDialog();
                    new SketchwareUtils().deleteTemp();
                    file_msg.setText(Environment.getExternalStorageDirectory() + "/Documents/CodeKosh/Sketchware Projects/Export/" + finalS + ".zip");

                }
            });
            dialog.dismiss();
        });

        dialog.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void remove(int position) {
        data_screenshots.remove(position);
        Objects.requireNonNull(recycler_screenshots.getAdapter()).notifyDataSetChanged();
    }

    public static class List1Adapter extends BaseAdapter {
        ArrayList<HashMap<String, Object>> _data;

        public List1Adapter(ArrayList<HashMap<String, Object>> _arr) {
            _data = _arr;
        }

        @Override
        public int getCount() {
            return _data.size();
        }

        @Override
        public HashMap<String, Object> getItem(int _index) {
            return _data.get(_index);
        }

        @Override
        public long getItemId(int _index) {
            return _index;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                convertView = inflater.inflate(R.layout.project_cus, parent, false);
            }

            final LinearLayout linear1 = convertView.findViewById(R.id.linear1);
            final ImageView icon = convertView.findViewById(R.id.icon);
            final TextView title = convertView.findViewById(R.id.title);
            final TextView pack = convertView.findViewById(R.id.pack);
            final TextView num = convertView.findViewById(R.id.num);

            HashMap<String, Object> map = _data.get(position);

            if (map.containsKey("sc_id")) {
                title.setText(String.valueOf(map.get("my_app_name")));
                pack.setText(String.valueOf(map.get("my_sc_pkg_name")));
                num.setText(String.valueOf(map.get("sc_id")));

                boolean hasCustomIcon = String.valueOf(map.get("custom_icon")).equals("true");
                int defaultIconResource = R.drawable.android;

                if (hasCustomIcon) {
                    String iconPath = FileUtil.getExternalStorageDir() + "/.sketchware/resources/icons/" + map.get("sc_id") + "/icon.png";
                    if (FileUtil.isExistFile(iconPath)) {
                        Bitmap bitmap = FileUtil.decodeSampleBitmapFromPath(iconPath, 1024, 1024);
                        if (bitmap != null) {
                            icon.setImageBitmap(bitmap);
                        } else {
                            icon.setImageResource(defaultIconResource);
                        }
                    } else {
                        icon.setImageResource(defaultIconResource);
                    }
                } else {
                    icon.setImageResource(defaultIconResource);
                }

                linear1.setVisibility(View.VISIBLE);
            } else {
                linear1.setVisibility(View.GONE);
            }
            return convertView;
        }


    }

    public class Memories_listAdapter extends RecyclerView.Adapter<Memories_listAdapter.ViewHolder> {
        private final ArrayList<HashMap<String, Object>> data;

        public Memories_listAdapter(ArrayList<HashMap<String, Object>> dataList) {
            data = dataList;
        }

        @NonNull
        @Override
        public Memories_listAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.screenshot_upload_cell, parent, false);
            return new Memories_listAdapter.ViewHolder(view);
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onBindViewHolder(@NonNull Memories_listAdapter.ViewHolder holder, int position) {
            View itemView = holder.itemView;
            TextView name = holder.itemView.findViewById(R.id.text);
            ImageView img = holder.itemView.findViewById(R.id.img);
            LinearLayout rex = holder.itemView.findViewById(R.id.rex);
            ImageView img2 = holder.itemView.findViewById(R.id.img2);
            String path = String.valueOf(data.get(position).get("path"));
            String nameStr = String.valueOf(data.get(position).get("name"));

            int nightModeFlags = itemView.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            name.setTextColor(nightModeFlags == Configuration.UI_MODE_NIGHT_YES ? Color.parseColor("#FFFFFF") : Color.parseColor("#222222"));


            rex.setOnClickListener(view -> {
                if (String.valueOf(position).equals("0")) {
                    if (data_screenshots.size() >= 6) {
                        showToast("You can add only 5 photos");
                    } else {
                        pickMultiplePhoto(list -> {
                            int itemsToAdd = Math.min(6 - data_screenshots.size(), list.size()); // Calculate how many items can be added

                            for (int i = 0; i < itemsToAdd; i++) {
                                if (new_project) {
                                    data_screenshots.add(list.get(i)); // Add items from the original list to ss
                                } else {
                                    data_screenshots.add(list.get(i)); // Add items from the original list to ss
                                    data_screenshots2.add(list.get(i)); // Add items from the original list to ss
                                }
                            }
                            Objects.requireNonNull(recycler_screenshots.getAdapter()).notifyDataSetChanged();
                        });
                    }
                } else {
                    remove(position);
                }
            });

            if (!path.equals("empty")) {
                if (path.contains("https")) {
                    Glide.with(holder.itemView.getContext()).load(Uri.parse(path)).into(img);
                } else {
                    Glide.with(holder.itemView.getContext()).load(path).into(img);
                }
            }
            name.setText(nameStr);
            if (!nameStr.equals("Add photos")) {
                img2.setImageResource(R.drawable.close);
            } else {
                img2.setImageResource(R.drawable.add_circle);
                img.setImageResource(R.drawable.file_image);
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(View view) {
                super(view);
            }
        }
    }
}