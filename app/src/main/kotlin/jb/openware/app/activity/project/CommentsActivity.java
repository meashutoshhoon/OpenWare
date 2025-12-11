package in.afi.codekosh.activity.project;

import static jb.openware.app.util.StringUtilsKt.moderatorUrl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.util.Linkify;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.afi.codekosh.R;
import in.afi.codekosh.activity.profile.ProfileActivity;
import in.afi.codekosh.dialogs.ReportDialog;
import in.afi.codekosh.tools.AndroidUtils;
import in.afi.codekosh.tools.BaseFragment;
import in.afi.codekosh.tools.ThemeBuilder;

public class CommentsActivity extends BaseFragment {
    private final FirebaseDatabase _firebase = FirebaseDatabase.getInstance();
    private final DatabaseReference comment = _firebase.getReference("comments");
    private final HashMap<String, Object> user_names = new HashMap<>();
    private final DatabaseReference users = _firebase.getReference("Users");
    private final Calendar time2 = Calendar.getInstance();
    private final ArrayList<HashMap<String, Object>> listmap = new ArrayList<>();
    private final HashMap<String, Object> badge = new HashMap<>();
    private final HashMap<String, Object> verified = new HashMap<>();
    private final HashMap<String, Object> colors = new HashMap<>();
    private final HashMap<String, Object> uid_list = new HashMap<>();
    private Calendar c = Calendar.getInstance();
    private String key = "";
    private HashMap<String, Object> map = new HashMap<>();
    private TextView title;
    private String post_key = "";
    private ChildEventListener _users_child_listener;
    private ImageView imageview2;
    private EditText edittext1;
    private ListView listview1;
    private SharedPreferences developer;
    private ArrayList<String> moderator_id = new ArrayList<>();
    private ImageView back;

    @Override
    protected boolean isHomeFragment() {
        return false;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_comments;
    }

    @Override
    public void getThemeDescriptions(ThemeBuilder themeBuilder) {
        themeBuilder.setImageColorFilter(back, BLACK, WHITE);
        themeBuilder.setTextColor(title, BLACK, WHITE);
    }

    @Override
    protected void initialize() {
        back = findViewById(R.id.back);
        title = findViewById(R.id.title);
        imageview2 = findViewById(R.id.imageview2);
        edittext1 = findViewById(R.id.edittext1);
        listview1 = findViewById(R.id.listview1);
        editText(this, edittext1);
        developer = getSharedPreferences("developer", Activity.MODE_PRIVATE);

        imageview2.setOnClickListener(_view -> {
            performHapticFeedback(imageview2);
            if (!edittext1.getText().toString().trim().equals("")) {
                c = Calendar.getInstance();
                key = comment.push().getKey();
                map = new HashMap<>();
                map.put("uid", getUID());
                map.put("post_key", post_key);
                map.put("key", key);
                map.put("time", String.valueOf(c.getTimeInMillis()));
                map.put("message", edittext1.getText().toString());
                comment.child(key).updateChildren(map);
                map.clear();
                edittext1.setText("");
                users.addChildEventListener(_users_child_listener);
            }
        });
        ChildEventListener _comment_child_listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                if (_childValue != null && post_key.equals(String.valueOf(_childValue.get("post_key")))) {
                    listmap.add(_childValue);
                    Listview1Adapter adapter = new Listview1Adapter(listmap);
                    listview1.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    users.addChildEventListener(_users_child_listener);
                }

                toggle(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                if (_childValue != null && post_key.equals(String.valueOf(_childValue.get("post_key")))) {
                    for (int i = 0; i < listmap.size(); i++) {
                        if (String.valueOf(listmap.get(i).get("key")).equals(_childKey)) {
                            listmap.set(i, _childValue);
                            break;
                        }
                    }
                    listview1.setAdapter(new Listview1Adapter(listmap));
                    ((BaseAdapter) listview1.getAdapter()).notifyDataSetChanged();
                    users.addChildEventListener(_users_child_listener);
                }

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot _param1, String _param2) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot _param1) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                if (_childValue != null && post_key.equals(String.valueOf(_childValue.get("post_key")))) {
                    listmap.removeIf(item -> String.valueOf(item.get("key")).equals(_childKey));
                    listview1.setAdapter(new Listview1Adapter(listmap));
                    ((BaseAdapter) listview1.getAdapter()).notifyDataSetChanged();
                    users.addChildEventListener(_users_child_listener);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError _param1) {

            }
        };
        comment.addChildEventListener(_comment_child_listener);

        _users_child_listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                if (_childValue != null && _childValue.containsKey("uid")) {
                    String uid = (String) _childValue.get("uid");

                    if (_childValue.containsKey("name")) {
                        user_names.put(uid, _childValue.get("name"));
                        uid_list.put((String) _childValue.get("name"), uid);
                    }

                    if (_childValue.containsKey("avatar")) {
                        map.put(uid, _childValue.get("avatar"));
                    }

                    if (_childValue.containsKey("badge")) {
                        badge.put(uid, _childValue.get("badge"));
                    }

                    if (_childValue.containsKey("verified")) {
                        verified.put(uid, _childValue.get("verified"));
                    }

                    if (_childValue.containsKey("color")) {
                        colors.put(uid, _childValue.get("color"));
                    }
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                if (_childValue != null && _childValue.containsKey("uid")) {
                    String uid = (String) _childValue.get("uid");

                    if (_childValue.containsKey("name")) {
                        user_names.put(uid, _childValue.get("name"));
                        uid_list.put((String) _childValue.get("name"), uid);
                    }

                    if (_childValue.containsKey("avatar")) {
                        map.put(uid, _childValue.get("avatar"));
                    }

                    if (_childValue.containsKey("badge")) {
                        badge.put(uid, _childValue.get("badge"));
                    }

                    if (_childValue.containsKey("verified")) {
                        verified.put(uid, _childValue.get("verified"));
                    }
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

        getConnectionsManager(moderatorUrl, GET, new RequestListener() {
            @Override
            public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {
                ArrayList<HashMap<String, Object>> data = new Gson().fromJson(response, new TypeToken<ArrayList<HashMap<String, Object>>>() {
                }.getType());
                if (!data.isEmpty()) {
                    moderator_id = new ArrayList<>();
                    for (HashMap<String, Object> item : data) {
                        String uidValue = (String) item.get("uid");
                        if (uidValue != null) {
                            moderator_id.add(uidValue);
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse(String tag, String message) {

            }
        });
    }

    @Override
    protected void initializeLogic() {
        toggle(true);
        back.setOnClickListener(v -> goBack());
        title.setText(String.valueOf(getIntent().getStringExtra("title")));
        post_key = getIntent().getStringExtra("key");
        listview1.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listview1.setStackFromBottom(true);
        listview1.setOnItemLongClickListener((parent, view, position, id) -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle("Options");
            String[] options;
            if (moderator_id.contains(getUID()) || String.valueOf(listmap.get(position).get("uid")).equals(getUID())) {
                options = new String[]{"View Profile", "Copy", "Delete", "Report comment"};
            } else {
                options = new String[]{"View Profile", "Copy", "Report comment"};
            }
            builder.setItems(options, (dialog, which) -> {
                switch (options[which]) {
                    case "View Profile":
                        developer.edit().putString("uid", String.valueOf(listmap.get(position).get("uid"))).apply();
                        startActivity(ProfileActivity.class);
                        break;
                    case "Copy":
                        AndroidUtils.copyText(this, String.valueOf(listmap.get(position).get("message")));
                        break;
                    case "Delete":
                        comment.child(String.valueOf(listmap.get(position).get("key"))).removeValue();
                        break;
                    case "Report comment":
                        new ReportDialog().showDialog(this, listmap.get(position));
                        break;
                }
            });
            builder.show();
            return true;
        });
    }

    private void toggle(boolean bool) {
        TransitionManager.beginDelayedTransition(findViewById(R.id.refer));
        if (bool) {
            showViews(findViewById(R.id.linear_shimmer));
            hideViews(findViewById(R.id.listview1));
            return;
        }
        showViews(findViewById(R.id.listview1));
        hideViews(findViewById(R.id.linear_shimmer));
    }

    public void detectLinks(final TextView textView) {
        textView.setClickable(true);
        Linkify.addLinks(textView, Linkify.ALL);
        textView.setLinkTextColor(Color.parseColor("#2196F3"));
        textView.setLinksClickable(true);
    }

    @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
    public void setTime(final double _currentTime, final TextView _txt) {
        double tm_difference = c.getTimeInMillis() - _currentTime;
        if (tm_difference < 60000) {
            if ((tm_difference / 1000) < 2) {
                _txt.setText("1 second ago");
            } else {
                _txt.setText(String.valueOf((long) (tm_difference / 1000)).concat(" seconds ago"));
            }
        } else {
            if (tm_difference < (60 * 60000)) {
                if ((tm_difference / 60000) < 2) {
                    _txt.setText("1 minute ago");
                } else {
                    _txt.setText(String.valueOf((long) (tm_difference / 60000)).concat(" minute ago"));
                }
            } else {
                if (tm_difference < (24 * (60 * 60000))) {
                    if ((tm_difference / (60 * 60000)) < 2) {
                        _txt.setText(String.valueOf((long) (tm_difference / (60 * 60000))).concat(" hours ago"));
                    } else {
                        _txt.setText(String.valueOf((long) (tm_difference / (60 * 60000))).concat(" hours ago"));
                    }
                } else {
                    if (tm_difference < (7 * (24 * (60 * 60000)))) {
                        if ((tm_difference / (24 * (60 * 60000))) < 2) {
                            _txt.setText(String.valueOf((long) (tm_difference / (24 * (60 * 60000)))).concat(" days ago"));
                        } else {
                            _txt.setText(String.valueOf((long) (tm_difference / (24 * (60 * 60000)))).concat(" days ago"));
                        }
                    } else {
                        time2.setTimeInMillis((long) (_currentTime));
                        _txt.setText(new SimpleDateFormat("dd MMM yyyy").format(time2.getTime()));
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public void editText(Activity activity, final TextView textView) {
        final TextView regex1 = new TextView(activity);
        regex1.setText("(?<![^\\s])(([@]{1}|[#]{1})([A-Za-z0-9_-]\\.?)+)(?![^\\s,])");
        final String mentionColor = "#2196F3";
        textView.addTextChangedListener(new TextWatcher() {
            final ColorScheme keywords1 = new ColorScheme(Pattern.compile(regex1.getText().toString()), Color.parseColor(mentionColor));
            final ColorScheme[] schemes = {keywords1};

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                removeSpans(s);
                for (ColorScheme scheme : schemes) {
                    for (Matcher m = scheme.pattern.matcher(s); m.find(); ) {
                        s.setSpan(new ForegroundColorSpan(scheme.color), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        s.setSpan(new StyleSpan(Typeface.BOLD), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    }
                }
            }

            void removeSpans(Editable e) {
                CharacterStyle[] spans = (CharacterStyle[]) e.getSpans(0, e.length(), (Class) ForegroundColorSpan.class);
                for (CharacterStyle span : spans) {
                    e.removeSpan(span);
                }
            }

            class ColorScheme {
                final Pattern pattern;
                final int color;

                ColorScheme(Pattern pattern, int color) {
                    this.pattern = pattern;
                    this.color = color;
                }
            }
        });
    }

    public void textView(final TextView textView) {
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        updateSpan(textView);
    }

    private void updateSpan(TextView textView) {
        SpannableString ssb = new SpannableString(textView.getText().toString());
        Pattern pattern = Pattern.compile("(?<![^\\s])(([@]{1}|[#]{1})([A-Za-z0-9_-]\\.?)+)(?![^\\s,])");
        Matcher matcher = pattern.matcher(textView.getText().toString());
        while (matcher.find()) {
            ProfileSpan span = new ProfileSpan();
            ssb.setSpan(span, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setText(ssb);

    }

    private class ProfileSpan extends ClickableSpan {

        @Override
        public void onClick(@NonNull View view) {
            if (view instanceof TextView) {
                TextView tv = (TextView) view;
                if (tv.getText() instanceof Spannable) {
                    Spannable sp = (Spannable) tv.getText();
                    int start = sp.getSpanStart(this);
                    int end = sp.getSpanEnd(this);
                    String object_clicked = sp.subSequence(start, end).toString();
                    if (object_clicked.contains("@")) {
                        String ui = String.valueOf(uid_list.get(object_clicked.substring(1)));
                        developer.edit().putString("uid", ui).apply();
                        startActivity(ProfileActivity.class);
                    }
                }
            }

        }

        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            ds.setUnderlineText(false);
            ds.setColor(Color.parseColor("#2196F3"));
        }
    }

    public class Listview1Adapter extends BaseAdapter {

        ArrayList<HashMap<String, Object>> _data;

        public Listview1Adapter(ArrayList<HashMap<String, Object>> _arr) {
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
        public View getView(final int position, View _v, ViewGroup _container) {
            LayoutInflater _inflater = getLayoutInflater();
            View _view = _v;
            if (_view == null) {
                _view = _inflater.inflate(R.layout.comments_cell, null);
            }
            HashMap<String, Object> hashMap = _data.get(position);

            final de.hdodenhof.circleimageview.CircleImageView circleImageView = _view.findViewById(R.id.circleimageview1);
            final LinearLayout linear_word = _view.findViewById(R.id.linear_word);
            final TextView message = _view.findViewById(R.id.message);
            final TextView name = _view.findViewById(R.id.name);
            final TextView time = _view.findViewById(R.id.time);
            final TextView tx_word = _view.findViewById(R.id.tx_word);
            final ImageView badge_img = _view.findViewById(R.id.badge);

            message.setTextColor(isNightMode() ? WHITE : TEXT_BLACK);
            name.setTextColor(isNightMode() ? WHITE : BLACK);
            time.setTextColor(isNightMode() ? TEXT_GREY : GREY);


            if (hashMap.containsKey("message") && hashMap.get("message") != null) {
                message.setText(String.valueOf(hashMap.get("message")));
//                TextFormatter.formatText(message, String.valueOf(hashMap.get("message")));
                detectLinks(message);
            }

            if (hashMap.containsKey("time") && hashMap.get("time") != null) {
                setTime(Double.parseDouble(String.valueOf(hashMap.get("time"))), time);
            }

            if (hashMap.containsKey("uid")) {
                String uid = String.valueOf(hashMap.get("uid"));
                if (user_names.containsKey(uid) && map.containsKey(uid) && colors.containsKey(uid)) {
                    name.setText(String.valueOf(user_names.get(uid)));
                    if (map.get(uid) != null) {
                        if (String.valueOf(map.get(uid)).equals("none")) {
                            hideViews(circleImageView);
                            showViews(linear_word);
                            tx_word.setText(String.valueOf(user_names.get(uid)).substring(0, 1));
                        } else {
                            hideViews(linear_word);
                            showViews(circleImageView);
                            Glide.with(getApplicationContext()).load(Uri.parse(String.valueOf(map.get(uid)))).into(circleImageView);
                        }
                    }
                    linear_word.setBackground(new GradientDrawable() {
                        public GradientDrawable getIns(int a, int b) {
                            this.setCornerRadius(a);
                            this.setColor(b);
                            return this;
                        }
                    }.getIns(360, Color.parseColor(String.valueOf(colors.get(uid)))));
                }
            }
            Object uidObject = hashMap.get("uid");
            if (uidObject != null) {
                String uid = String.valueOf(uidObject);
                Object badgeValue = badge.get(uid);
                Object verifiedValue = verified.get(uid);

                if (badgeValue != null && verifiedValue != null) {
                    final int NO_BADGE = 0;
                    int badgeInt = Integer.parseInt(String.valueOf(badgeValue));
                    boolean isVerified = Boolean.parseBoolean(String.valueOf(verifiedValue));

                    if (badgeInt == NO_BADGE) {
                        if (isVerified) {
                            badge_img.setImageResource(R.drawable.verify);
                            badge_img.setColorFilter(0xFF00C853, PorterDuff.Mode.SRC_IN);
                            name.setTextColor(0xFF00C853);
                        } else {
                            hideViews(badge_img);
                        }
                    } else {
                        new BadgeDrawable(CommentsActivity.this).setBadge(String.valueOf(badgeInt), badge_img);
                        int colorFilter = isNightMode() ? 0xFF8DCDFF : 0xFF006493;
                        name.setTextColor(colorFilter);
                    }
                } else {
                    hideViews(badge_img);
                }
            }

            name.setOnClickListener(v -> circleImageView.performClick());

            circleImageView.setOnClickListener(v1 -> {
                developer.edit().putString("uid", getUID()).apply();
                startActivity(ProfileActivity.class);
            });

            return _view;
        }
    }


}