package in.afi.codekosh.tools;

import static in.afi.codekosh.tools.StringUtilsKt.email;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.StateSet;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.afi.codekosh.components.AppConfig;
import in.afi.codekosh.components.FolderManagement;
import in.afi.codekosh.components.LoadingDialog;

public abstract class BaseFragment extends AppCompatActivity {
    protected static final int WHITE = 0xFFFFFFFF;
    protected static final int BLACK = 0xFF000000;
    protected static final int TEXT_BLACK = 0xFF222222;
    protected static final int TEXT_WHITE = 0xFFDDDDDD;
    protected static final int TEXT_GREY = 0xFF989FA7;
    protected static final int GREY = 0xFF757575;

    protected static final String SUBSCRIPTION = "subscription";
    protected static final String GET = "GET";
    private static final int REQUEST_CODE_PICK_IMAGES = 1;
    private static final int STORAGE_PERMISSION_CODE_NEW = 100;
    public static String swb = "swb";
    public static String zip = "zip";
    protected static float density = 1;
    protected final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private PermissionListener permissionListener;
    protected final ActivityResultLauncher<Intent> storageActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || !Environment.isExternalStorageManager()) {
            if (permissionListener != null) {
                permissionListener.onNotGranted();
            }
        } else {
            if (permissionListener != null) {
                permissionListener.onGranted();
            }
        }
    });
    private Vibrator vibrator;
    private Runnable delayedTask;
    private MProgressDialog mProgressDialog;
    private LoadingDialog loadingDialog;
    private BottomSheetDialog bottomSheetDialog;
    private ImagePickedListener listener;
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if (result != null) {
            // Handle the picked image URI
            ContentResolver contentResolver = getContentResolver();
            String imageFileName = getImageFileName(contentResolver, result);

            String profilePath = convertUriToFilePath(this, result);

            if (listener != null) {
                listener.onImagePicked(profilePath, imageFileName, result);
            }
        }
    });
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri uri) {
            if (uri != null) {
                ContentResolver contentResolver = getContentResolver();
                String imageFileName = getImageFileName(contentResolver, uri);
                String profilePath = convertUriToFilePath(BaseFragment.this, uri);

                if (listener != null) {
                    listener.onImagePicked(profilePath, imageFileName, uri);
                }
            } else {
                Log.d("PhotoPicker", "No media selected");
            }
        }
    });
    private long vibrationDuration;
    private VideoPickedListener listener_v;
    private final ActivityResultLauncher<String> pickImageLauncher_r = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if (result != null) {
            // Handle the picked image URI
            ContentResolver contentResolver = getContentResolver();
            String imageFileName = getVideoFileName(contentResolver, result);

            String profilePath = convertUriToFilePath(this, result);

            if (listener_v != null) {
                listener_v.onVideoPicked(profilePath, imageFileName, result);
            }
        }
    });
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia_v = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
        if (uri != null) {
            ContentResolver contentResolver = getContentResolver();
            String imageFileName = getVideoFileName(contentResolver, uri);
            String profilePath = convertUriToFilePath(BaseFragment.this, uri);

            if (listener_v != null) {
                listener_v.onVideoPicked(profilePath, imageFileName, uri);
            }
        } else {
            Log.d("PhotoPicker", "No media selected");
        }
    });
    private MultipleImagePickedListener listener_m;
    private FilePickedListener filePickedListener;
    private ArrayList<HashMap<String, Object>> photos;
    ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia = registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(5), uris -> {
        if (!uris.isEmpty()) {
            for (Uri uri : uris) {
                String profilePath;
                profilePath = convertUriToFilePath(this, uri);
                ContentResolver contentResolver = getContentResolver();
                String imageFileName = getImageFileName(contentResolver, uri);
                HashMap<String, Object> item = new HashMap<>();
                item.put("path", profilePath);
                item.put("name", imageFileName);
                photos.add(item);
            }

            listener_m.onImagePicked(photos);
        } else {
            Log.d("PhotoPicker", "No media selected");
        }
    });
    private ChildEventListener ChildEventListener;
    private DatabaseReference databaseReference;

    protected static int getRandom(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    protected static Drawable createRoundRectDrawable(int radius, int defaultColor) {
        ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(new float[]{radius, radius, radius, radius, radius, radius, radius, radius}, null, null));
        defaultDrawable.getPaint().setColor(defaultColor);
        return defaultDrawable;
    }

    protected static Drawable getRoundRectSelectorDrawable(int color) {
        Drawable maskDrawable = createRoundRectDrawable2(dp(15, true), 0xffffffff);
        ColorStateList colorStateList = new ColorStateList(new int[][]{StateSet.WILD_CARD}, new int[]{(color & 0x00ffffff) | 0x19000000});
        return new RippleDrawable(colorStateList, null, maskDrawable);
    }

    protected static Drawable createRoundRectDrawable2(int rad, int defaultColor) {
        ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(new float[]{rad, rad, rad, rad, rad, rad, rad, rad}, null, null));
        defaultDrawable.getPaint().setColor(defaultColor);
        return defaultDrawable;
    }

    protected static int dp(float value, boolean bl) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }

    public static String convertUriToFilePath(final Context context, final Uri uri) {
        String path = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    path = Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);

                if (!TextUtils.isEmpty(id)) {
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "");
                    }
                }

                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                path = getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                path = getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if (ContentResolver.SCHEME_CONTENT.equalsIgnoreCase(uri.getScheme())) {
            path = getDataColumn(context, uri, null, null);
        } else if (ContentResolver.SCHEME_FILE.equalsIgnoreCase(uri.getScheme())) {
            path = uri.getPath();
        }

        if (path != null) {
            try {
                return URLDecoder.decode(path, StandardCharsets.UTF_8);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        final String column = MediaStore.Images.Media.DATA;
        final String[] projection = {column};

        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception ignored) {

        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    protected GradientDrawable createGradientDrawable(int cornerRadius, int strokeWidth, int strokeColor, int fillColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(cornerRadius);
        drawable.setStroke(strokeWidth, strokeColor);
        drawable.setColor(fillColor);
        return drawable;
    }

    protected int dp(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private String getVideoFileName(ContentResolver contentResolver, Uri videoUri) {
        String[] projection = {MediaStore.Video.Media.DISPLAY_NAME};
        Cursor cursor = contentResolver.query(videoUri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
            String fileName = cursor.getString(columnIndex);
            cursor.close();
            return fileName;
        }
        return null;
    }

    public boolean isNightMode() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    protected boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int write = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);

            return write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED;
        }
    }

    protected void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", this.getPackageName(), null);
            intent.setData(uri);
            try {
                storageActivityResultLauncher.launch(intent);
            } catch (Exception e) {
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                storageActivityResultLauncher.launch(intent);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE_NEW);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE_NEW && grantResults.length == 2) {
            boolean writePermissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean readPermissionGranted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

            if (!(writePermissionGranted && readPermissionGranted)) {
                if (permissionListener != null) {
                    permissionListener.onNotGranted();
                }
            } else {
                if (permissionListener != null) {
                    permissionListener.onGranted();
                }
            }
        }
    }

    protected void checkExpirationTime(TimeCallback timeCallback) {
        // Get the current time
        Date currentTime = Calendar.getInstance().getTime();

        // Convert the current time to a string
        String currentTimeString = dateTimeFormat.format(currentTime);

        // Get the database reference

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("expiration");

        ChildEventListener _users_child_listener = new ChildEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChildAdded(DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> userData = _param1.getValue(_ind);
                if (_childKey.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    if (_childKey.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        timeCallback.onDateChangedListener(userData);
                    }
                } else {
                    timeCallback.onNoUser();
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onChildChanged(DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> userData = _param1.getValue(_ind);
                if (_childKey.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    if (_childKey.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        timeCallback.onDateChangedListener(userData);
                    }
                } else {
                    timeCallback.onNoUser();
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
                alertCreator(_param1.getMessage());
            }
        };
        databaseRef.addChildEventListener(_users_child_listener);

        // Retrieve the expiration time from the database
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutRes());
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrationDuration = 250;
        initializeActivity();
        initialize();
        initializeLogic();
        new FolderManagement().makeFolders();
    }

    public int dpi(float value) {
        return (int) (this.getResources().getDisplayMetrics().density * value);
    }

    protected void checkStorage(PermissionListener listener) {
        permissionListener = listener;
        if (!checkPermission()) {
            requestPermission();
        } else {
            permissionListener.onGranted();
        }
    }

    public void sendEmail(final String subject, final String body) {
        String emailSubject = Uri.encode(subject);
        String emailBody = Uri.encode(body);

        String mailTo = "mailto:" + email + "?&subject=" + emailSubject + "&body=" + emailBody;

        Intent emailIntent = new Intent(Intent.ACTION_VIEW);
        emailIntent.setData(Uri.parse(mailTo));

        startActivity(emailIntent);
    }

    protected void openKeyboard(EditText editText) {
        if (editText != null) {
            editText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    protected void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    protected String getMessagingToken() {
        final String[] token = new String[1];

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                token[0] = task.getResult();
            }
        });

        return token[0];
    }

    protected void initializeActivity() {
        FirebaseApp.initializeApp(this);
        initializeActivity3();
        if (isHomeFragment()) {
            getWindow().setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(this));
        }
    }

    public Activity getParentActivity() {
        return this;
    }

    private void initializeActivity3() {
        FirebaseApp.initializeApp(this);
        if (isHomeFragment()) {
            InitializeActivity.getInstance().initializeActivity(this);
        } else {
            InitializeActivity.getInstance().initializeActivity2(this);
        }
    }

    protected void hideViews(View... views) {
        for (View view : views) {
            view.setVisibility(View.GONE);
        }
    }

    protected void showViews(View... views) {
        for (View view : views) {
            view.setVisibility(View.VISIBLE);
        }
    }

    protected void hideKeyboard(Context _context) {
        InputMethodManager _inputMethodManager = (InputMethodManager) _context.getSystemService(Context.INPUT_METHOD_SERVICE);
        _inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }


    protected void restartApp() {
        finishAffinity();
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        startActivity(intent);

    }

    protected void createBottomSheetDialog(@LayoutRes int layoutRes) {
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(layoutRes);
    }

    protected void showBottomSheetDialog() {
        if (bottomSheetDialog != null) {
            bottomSheetDialog.show();
        }
    }

    protected void setBottomSheetCancelable(boolean cancelable) {
        if (bottomSheetDialog != null) {
            bottomSheetDialog.setCancelable(cancelable);
        }
    }

    protected void dismissBottomSheetDialog() {
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
        }
    }

    protected View bsId(@IdRes int viewId) {
        if (bottomSheetDialog != null) {
            return bottomSheetDialog.findViewById(viewId);
        }
        return null;
    }

    protected void getProjectId(String key, boolean premium, FirebaseDataCallback callback) {
        String databasePath = premium ? "projects/premium" : "projects/normal";
        DatabaseReference usersDatabaseRef = FirebaseDatabase.getInstance().getReference(databasePath);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && Objects.equals(dataSnapshot.getKey(), key)) {
                    HashMap<String, Object> userData = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Object>>() {
                    });
                    if (userData != null) {
                        String value = String.valueOf(userData.get("id"));
                        callback.onDataRetrieved(value);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event, if needed
                callback.onDataRetrieved("error");
            }
        };
        usersDatabaseRef.child(key).addListenerForSingleValueEvent(valueEventListener);
    }

    protected void storeExpirationTimeInDatabase(String s) {
        // Get the current time
        Date currentTime = Calendar.getInstance().getTime();

        // Add 24 hours to the current time
        Calendar expirationCalendar = Calendar.getInstance();
        expirationCalendar.setTime(currentTime);
        expirationCalendar.add(Calendar.HOUR_OF_DAY, 24);
        Date expirationTime = expirationCalendar.getTime();

        // Convert the dates to strings
        String currentTimeString = dateTimeFormat.format(currentTime);
        String expirationTimeString = dateTimeFormat.format(expirationTime);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(s, expirationTimeString);

        pushToDatabase(hashMap, "expiration", getUserConfig().getUid(), unused -> {

        }, e -> alertCreator(e.getMessage()));
    }

    protected void storeExpirationTimeInDatabase(String s, int numberOfDays) {
        // Get the current time
        Date currentTime = Calendar.getInstance().getTime();

        // Add the specified number of days to the current time
        Calendar expirationCalendar = Calendar.getInstance();
        expirationCalendar.setTime(currentTime);
        expirationCalendar.add(Calendar.DAY_OF_MONTH, numberOfDays);
        Date expirationTime = expirationCalendar.getTime();

        // Convert the dates to strings
        String currentTimeString = dateTimeFormat.format(currentTime);
        String expirationTimeString = dateTimeFormat.format(expirationTime);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(s, expirationTimeString);

        pushToDatabase(hashMap, "expiration", getUserConfig().getUid(), unused -> {

        }, e -> alertCreator(e.getMessage()));
    }

    protected void pushToDatabase(HashMap<String, Object> dataMap, String reference, String child, OnSuccessListener<Void> pushSuccessListener, OnFailureListener pushFailureListener) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(reference);
        databaseReference.child(child).updateChildren(dataMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Data pushed successfully
                Log.d("Firebase", "Data pushed to the database successfully");
                pushSuccessListener.onSuccess(null);
            } else {
                // Failed to push data
                pushFailureListener.onFailure(Objects.requireNonNull(task.getException()));
                Log.e("Firebase", "Failed to push data to the database: " + task.getException());
            }
        });
    }

    protected void retrieveProjectsListFromFirebase(FirebaseDataCallback callback) {
        String currentUserId = "pp_3";
        DatabaseReference usersDatabaseRef = FirebaseDatabase.getInstance().getReference("pp");

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && Objects.equals(dataSnapshot.getKey(), currentUserId)) {
                    HashMap<String, Object> userData = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Object>>() {
                    });
                    if (userData != null) {
                        String value = String.valueOf(userData.get("points"));
                        callback.onDataRetrieved(value);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event, if needed
                callback.onDataRetrieved("error");
            }
        };

        usersDatabaseRef.child(currentUserId).addListenerForSingleValueEvent(valueEventListener);
    }

    protected void updateProjectsListToFirebase(OnSuccessListener<Void> pushSuccessListener) {
        retrieveProjectsListFromFirebase(value -> {
            int currentPoints = Integer.parseInt(value);
            int updatedPoints = currentPoints + 1;
            HashMap<String, Object> dataMap = new HashMap<>();
            dataMap.put("points", String.valueOf(updatedPoints));
            String databasePath = "pp";
            pushToDatabase(dataMap, databasePath, "pp_3", pushSuccessListener, e -> alertCreator(e.getMessage()));
        });
    }

    protected abstract boolean isHomeFragment();

    protected void delayTask(Runnable task) {
        delayTask(task, 200);
    }

    protected void delayTask(Runnable task, long delayMillis) {
        Handler handler = new Handler();
        handler.postDelayed(task, delayMillis);
    }

    protected String getBaseUrl(String id) {
        return "https://ashutoshgupta01.github.io/vid/" + id + ".json";
    }

    protected void createLinkSpan(String firstText, String secondText, int layoutId, View.OnClickListener clickListener) {
        LinkSpan linkSpan = findViewById(layoutId);
        linkSpan.setFirstText(firstText);
        linkSpan.setSecondText(secondText);
        linkSpan.setFirstTextColor(0xFF757575);
        linkSpan.setSecondTextColor(0xff2678b6);
        linkSpan.setTextSize(13f);
        linkSpan.setOnClickListener(clickListener);
    }

    protected void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new MProgressDialog(this);
        }
        mProgressDialog.show();
    }

    protected void showLoadingDialog(String text) {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
        }
        loadingDialog.show(text);
    }

    /**
     * Dismiss the progress dialog if it is visible.
     */
    protected void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.hide();
        }
    }

    protected void dismissLoadingDialog() {
        if (loadingDialog != null) {
            loadingDialog.hide();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseReference != null && ChildEventListener != null) {
            databaseReference.removeEventListener(ChildEventListener);
        }
    }

    protected MaterialAlertDialogBuilder createDialog(String title, String message, String positiveButtonText, String negativeButtonText, boolean cancel, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener) {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(title).setMessage(message).setCancelable(cancel).setPositiveButton(positiveButtonText, positiveListener).setNegativeButton(negativeButtonText, negativeListener);

        return builder;

    }

    protected void uiThread(Runnable runnable) {
        if (isFinishing()) {
            return;
        }

        if (runnable != null) {
            runOnUiThread(runnable);
        }
    }

    protected void childEventListener(String referencePath, ChildListener listener) {
        final FirebaseDatabase _firebase = FirebaseDatabase.getInstance();
        databaseReference = _firebase.getReference(referencePath);
        ChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                listener.onChildAdded(snapshot, previousChildName);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                listener.onChildChanged(snapshot, previousChildName);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                listener.onChildRemoved(snapshot);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                listener.onChildMoved(snapshot, previousChildName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onCancelled(error);
            }
        };
        databaseReference.addChildEventListener(ChildEventListener);


    }

    protected void hideKeyboard(View rootView) {
        AndroidUtils.hideKeyboard(rootView);
    }

    protected void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    protected UserConfig getUserConfig() {
        return new UserConfig(this);
    }

    protected AppConfig getAppConfig() {
        return new AppConfig(this);
    }

    protected void alertCreator(String message) {
        alertCreator(message, null);
    }

    protected void alertCreator(String message, DialogInterface.OnClickListener positiveListener) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Alert").setMessage(message).setPositiveButton(android.R.string.ok, positiveListener).show();
    }

    protected void dismissDialog(AlertDialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    protected MaterialAlertDialogBuilder createDialog(String title, String message, String positiveButtonText, DialogInterface.OnClickListener positiveListener) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(title).setMessage(message).setPositiveButton(positiveButtonText, positiveListener);
        return builder;

    }

    protected void backgroundRunner(Runnable task) {
        ExecutorService service = Executors.newFixedThreadPool(5);
        service.execute(task);
    }

    protected void getConnectionsManager(String url, String method, final RequestListener requestListener) {
        RequestNetwork requestNetwork = new RequestNetwork(this);
        requestNetwork.startRequestNetwork(method, url, "", new RequestNetwork.RequestListener() {
            @Override
            public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {
                requestListener.onResponse(tag, response, responseHeaders);
            }

            @Override
            public void onErrorResponse(String tag, String message) {
                requestListener.onErrorResponse(tag, message);
            }
        });
    }

    protected void getConnectionsManager(String url, String method, HashMap<String, Object> headers, final RequestListener requestListener) {
        RequestNetwork requestNetwork = new RequestNetwork(this);
        requestNetwork.setHeaders(headers);
        requestNetwork.startRequestNetwork(method, url, "", new RequestNetwork.RequestListener() {
            @Override
            public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {
                requestListener.onResponse(tag, response, responseHeaders);
            }

            @Override
            public void onErrorResponse(String tag, String message) {
                requestListener.onErrorResponse(tag, message);
            }
        });
    }

    protected void showToast(String message) {
        showToast(message, false);
    }

    protected void performHapticFeedback(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    }

    protected void showToast(String message, boolean isLongDuration) {
        int duration = isLongDuration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        Toast.makeText(this, message, duration).show();
    }

    protected String getUID() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        return (user != null) ? user.getUid() : "Error";

    }

    protected void openActivity(Activity activityToOpen) {
        openActivity(activityToOpen, false);
    }

    protected void openActivity(Activity activityToOpen, boolean t) {
        // Create an Intent to open the desired activity
        Intent intent = new Intent(this, activityToOpen.getClass());
        if (t) {
            startActivity(intent);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

    }

    protected boolean checkAndroid13() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU;
    }

    public void pickSinglePhoto(final ImagePickedListener l) {
        if (checkAndroid13()) {
            pickMedia.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
        } else {
            pickImageLauncher.launch("image/*");
        }


        listener = l;
    }

    public void pickSingleVideo(final VideoPickedListener l) {
        if (checkAndroid13()) {
            pickMedia_v.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE).build());
        } else {
            pickImageLauncher_r.launch("video/*");
        }


        listener_v = l;
    }

    protected String generateFileNameWithTimestamp() {
        // Get the current user's UID
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get the current timestamp in milliseconds
        long timestamp = Calendar.getInstance().getTimeInMillis();

        // Generate the file name by concatenating the parts
        String fileName = "AVATAR_" + uid + "_" + timestamp + ".png";

        return fileName;
    }

    protected String generateProjectNameWithTimestamp() {
        // Get the current user's UID
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get the current timestamp in milliseconds
        long timestamp = Calendar.getInstance().getTimeInMillis();

        // Generate the file name by concatenating the parts

        return "p" + uid + "_" + timestamp;
    }

    protected void extractUidAndTimestamp(String fileName, UidTimestampListener listener) {
        // Extract the UID and timestamp from the file name
        String[] parts = fileName.split("_");
        String uid = parts[1];
        String timestamp = parts[2].substring(0, parts[2].indexOf(".png"));

        // Call the listener with the UID and timestamp
        if (listener != null) {
            listener.onUidTimestampGenerated(uid, timestamp);
        }
    }

    @SuppressLint("DefaultLocale")
    public String formatNumber(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        int number = Integer.parseInt(input);
        try {
            if (number >= 1000000000) {
                double billionValue = number / 1e9;
                return String.format("%.1fB", billionValue);
            } else if (number >= 1000000) {
                double millionValue = number / 1e6;
                return String.format("%.1fM", millionValue);
            } else if (number >= 1000) {
                double thousandValue = number / 1e3;
                return String.format("%.1fK", thousandValue);
            } else {
                return Integer.toString(number);
            }
        } catch (NumberFormatException e) {
            return Integer.toString(number);
        }
    }

    protected void alertToast(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(vibrationDuration, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(vibrationDuration);
        }
        showToast(text);
    }

    protected void uploadFileToFirebaseStorage(String storagePath, String child, Uri fileUri, OnSuccessListener<Uri> successListener, OnFailureListener failureListener) {
        // Create a Firebase Storage reference
        StorageReference storageRef = FirebaseStorage.getInstance().getReference(storagePath);

        // Create a child reference in the storage path
        StorageReference fileRef = storageRef.child(child);

        // Upload the file to Firebase Storage
        UploadTask uploadTask = fileRef.putFile(fileUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // File upload success
            // Get the download URL of the uploaded file
            fileRef.getDownloadUrl().addOnSuccessListener(successListener).addOnFailureListener(failureListener);
        }).addOnFailureListener(failureListener);
    }

    public void pickMultiplePhoto(final MultipleImagePickedListener l) {
        photos = new ArrayList<>();
        if (checkAndroid13()) {
            pickMultipleMedia.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(Intent.createChooser(intent, "Select Images"), REQUEST_CODE_PICK_IMAGES);
        }


        listener_m = l;
    }

    protected String stringFormat(Object object) {
        return String.valueOf(object);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_IMAGES && resultCode == RESULT_OK) {
            if (data != null) {
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    // Multiple images selected
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        if (photos.size() >= 6) {
                            // Limit reached, show a message or take appropriate action
                            Toast.makeText(this, "You can select only up to 5 images", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        Uri imageUri = clipData.getItemAt(i).getUri();
                        String profilePath = convertUriToFilePath(this, imageUri);
                        ContentResolver contentResolver = getContentResolver();
                        String imageFileName = getImageFileName(contentResolver, imageUri);

                        HashMap<String, Object> item = new HashMap<>();
                        item.put("path", profilePath);
                        item.put("name", imageFileName);

                        photos.add(item);
                    }
                    listener_m.onImagePicked(photos);
                } else {
                    // Single image selected
                    Uri imageUri = data.getData();
                    String profilePath = convertUriToFilePath(this, imageUri);
                    ContentResolver contentResolver = getContentResolver();
                    String imageFileName = getImageFileName(contentResolver, imageUri);

                    HashMap<String, Object> item = new HashMap<>();
                    item.put("path", profilePath);
                    item.put("name", imageFileName);

                    photos.add(item);
                    listener_m.onImagePicked(photos);
                }

            }
        }

        if (requestCode == 123 && resultCode == RESULT_OK) {
            if (data != null) {
                Uri imageUri = data.getData();
                String profilePath = convertUriToFilePath(this, imageUri);
                ContentResolver contentResolver = getContentResolver();
                String imageFileName = getImageFileName(contentResolver, imageUri);
                filePickedListener.onFilePicked(profilePath, imageFileName);
            }

        }
    }

    private String getImageFileName(ContentResolver contentResolver, Uri imageUri) {
        String[] projection = {MediaStore.Images.Media.DISPLAY_NAME};
        Cursor cursor = contentResolver.query(imageUri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            String fileName = cursor.getString(columnIndex);
            cursor.close();
            return fileName;
        }
        return null;
    }

    protected void goBack() {
        finish();
    }

    @LayoutRes
    protected abstract int getLayoutRes();

    protected void startActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }

    protected void syncTask(SyncTaskListener syncTaskListener) {
        syncTaskListener.beforeTaskStart();
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            syncTaskListener.onBackground();
            runOnUiThread(syncTaskListener::onTaskComplete);
        });

    }

    public String getFileExtension(String filePath) {
        String extension = "";

        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = filePath.substring(lastDotIndex);
        }

        return extension;
    }

    protected void pickFile(String s, FilePickedListener filePickedListener) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        if (Objects.equals(s, swb)) {
            intent.setType("*/*");  // Allow only .swb files to be picked.
        } else {
            intent.setType("application/zip");  // Allow only .zip files to be picked.
        }
        startActivityForResult(intent, 123);
        this.filePickedListener = filePickedListener;
    }

    public abstract void getThemeDescriptions(ThemeBuilder themeBuilder);

    @Override
    public void onResume() {
        super.onResume();
        if (delayedTask != null) {
            delayedTask.run();
            delayedTask = null; // Clear the task after running it
        }
        ThemeBuilder themeBuilder = createThemeBuilder();
        getThemeDescriptions(themeBuilder);
        activityLoad();
    }

    protected void activityLoad() {

    }

    protected void runnableTask(Runnable task) {
        new Handler(Looper.getMainLooper()).post(task);
    }

    protected void activityLoad(Runnable task) {
        delayedTask = task;
    }

    private ThemeBuilder createThemeBuilder() {
        return new ThemeBuilder(this) {
            @Override
            public void setValues(ThemeBuilder themeBuilder) {
                // Leave this method implementation empty in the base fragment
            }
        };
    }

    protected abstract void initialize();

    protected abstract void initializeLogic();

    protected interface ChildListener {
        void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName);

        void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName);

        void onChildRemoved(@NonNull DataSnapshot snapshot);

        void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName);

        void onCancelled(@NonNull DatabaseError error);
    }

    protected interface UidTimestampListener {
        void onUidTimestampGenerated(String uid, String timestamp);
    }

    protected interface SyncTaskListener {
        void beforeTaskStart();

        void onBackground();

        void onTaskComplete();
    }


    public interface ImagePickedListener {
        void onImagePicked(String profilePath, String imageFileName, Uri imageUri);
    }

    public interface FilePickedListener {
        void onFilePicked(String filePath, String fileName);
    }

    public interface VideoPickedListener {
        void onVideoPicked(String profilePath, String videoFileName, Uri videoUri);
    }

    public interface MultipleImagePickedListener {
        void onImagePicked(ArrayList<HashMap<String, Object>> list);
    }

    protected interface TimeCallback {
        void onDateListener(HashMap<String, Object> bool);

        void onDateChangedListener(HashMap<String, Object> bool);

        void onNoUser();
    }

    protected interface FirebaseDataCallback {
        void onDataRetrieved(String value);
    }

    protected interface RequestListener {
        void onResponse(String tag, String response, HashMap<String, Object> responseHeaders);

        void onErrorResponse(String tag, String message);
    }

    protected interface PermissionListener {
        void onGranted();

        void onNotGranted();
    }
}