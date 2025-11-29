package in.afi.codekosh.activity.home;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.google.android.gms.ads.AdLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.afi.codekosh.R;
import in.afi.codekosh.adapter.BannerProjectAdapter;
import in.afi.codekosh.adapter.BaseProjectAdapter;
import in.afi.codekosh.components.SharedPreferencesManager;
import in.afi.codekosh.nativeAds.MobileAdsLoader;
import in.afi.codekosh.nativeAds.TemplateView;

public class HomeFragment extends Fragment {
    private final FirebaseDatabase _firebase = FirebaseDatabase.getInstance();
    private final DatabaseReference normal = _firebase.getReference("projects/normal");
    private final double limit = 15;
    // simple boolean to check the status of ad
    private final boolean adLoaded = false;
    TemplateView template;
    private ArrayList<HashMap<String, Object>> editors_choice_projects = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> new_projects = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> most_projects = new ArrayList<>();
    private RecyclerView recyclerview_editors, recyclerview_latest, recyclerview_liked;
    private SharedPreferences developer;
    private String key = "";
    private View view;
    private boolean done = false;
    private AdLoader adLoader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        recyclerview_editors = view.findViewById(R.id.recyclerview_editors);
        recyclerview_latest = view.findViewById(R.id.recyclerview_latest);
        recyclerview_liked = view.findViewById(R.id.recyclerview_liked);
        developer = requireActivity().getSharedPreferences("developer", Activity.MODE_PRIVATE);
        recyclerview_editors.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerview_latest.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerview_liked.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        TextView t5 = view.findViewById(R.id.textview5);
        TextView t6 = view.findViewById(R.id.textview6);
        TextView t10 = view.findViewById(R.id.textview10);

        if (isNightMode()) {
            t5.setTextColor(0xFFFFFFFF);
            t6.setTextColor(0xFFFFFFFF);
            t10.setTextColor(0xFFFFFFFF);
        } else {
            t5.setTextColor(0xFF000000);
            t6.setTextColor(0xFF000000);
            t10.setTextColor(0xFF000000);
        }

        data1();
        hideViews(view.findViewById(R.id.recyclerview_editors));
        template = view.findViewById(R.id.nativeTemplateView);


        if (new SharedPreferencesManager(requireActivity()).getBoolean("ads", true)) {
            new MobileAdsLoader(requireActivity()).builtConfig().loadNativeAd(template, view.findViewById(R.id.base));
        }


        view.findViewById(R.id.editor_seemore).setOnClickListener(v -> {
            if (done) {
                new SharedPreferencesManager(requireActivity()).saveString("id", "editors_choice");
                requireActivity().startActivity(new Intent(requireActivity(), MoreProjectsActivity.class));
            }
        });
        view.findViewById(R.id.textview7).setOnClickListener(v -> {
            if (done) {
                new SharedPreferencesManager(requireActivity()).saveString("id", "all");
                requireActivity().startActivity(new Intent(requireActivity(), MoreProjectsActivity.class));
            }
        });
        view.findViewById(R.id.textview11).setOnClickListener(v -> {
            if (done) {
                new SharedPreferencesManager(requireActivity()).saveString("id", "like");
                requireActivity().startActivity(new Intent(requireActivity(), MoreProjectsActivity.class));
            }
        });
        transitionManager(view.findViewById(R.id.base), 400);
    }


    private void data1() {
        key = "editors_choice";
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Query query1 = normal.limitToLast((int) limit).orderByChild(key).startAt("true").endAt("true");
            query1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot _param1) {
                    delayTask(() -> data2(), 100);
                    done = true;
                    try {
                        editors_choice_projects = new ArrayList<>();
                        GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                        };

                        for (DataSnapshot _data : _param1.getChildren()) {
                            HashMap<String, Object> _map = _data.getValue(_ind);
                            String visibility = String.valueOf(_map.get("visibility"));
                            if (visibility.equals("true")) {
                                editors_choice_projects.add(_map);
                            }
                        }

                        Collections.reverse(editors_choice_projects);
                        requireActivity().runOnUiThread(() -> {
                            BannerProjectAdapter mostAdapter = new BannerProjectAdapter(editors_choice_projects, requireActivity(), 1);
                            recyclerview_editors.setAdapter(mostAdapter);
                            showViews(view.findViewById(R.id.recyclerview_editors));
                            hideViews(view.findViewById(R.id.linear_shimmer1));
                        });

                    } catch (Exception ignored) {
                        // Handle exceptions
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle cancellations
                }
            });
        });
    }

    private void data2() {
        key = "latest";
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Query query2 = normal.limitToLast((int) limit).orderByChild(key).startAt("true").endAt("true");
            query2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot _param1) {
                    delayTask(() -> data3());
                    try {
                        new_projects = new ArrayList<>();
                        GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                        };

                        for (DataSnapshot _data : _param1.getChildren()) {
                            HashMap<String, Object> _map = _data.getValue(_ind);
                            String visibility = String.valueOf(_map.get("visibility"));
                            if (visibility.equals("true")) {
                                new_projects.add(_map);
                            }
                        }

                        Collections.reverse(new_projects);
                        requireActivity().runOnUiThread(() -> {
                            BaseProjectAdapter mostAdapter = new BaseProjectAdapter(new_projects, requireActivity());
                            recyclerview_latest.setAdapter(mostAdapter);
                            hideViews(view.findViewById(R.id.linear_shimmer2));
                            showViews(view.findViewById(R.id.recyclerview_latest));
                        });
                    } catch (Exception ignored) {
                        // Handle exceptions
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle cancellations
                }
            });
        });
    }

    private void data3() {
        key = "visibility";
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Query query3 = normal.limitToLast((int) limit).orderByChild(key).startAt("true").endAt("true");
            query3.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot _param1) {
                    try {
                        most_projects = new ArrayList<>();
                        GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                        };

                        for (DataSnapshot _data : _param1.getChildren()) {
                            HashMap<String, Object> _map = _data.getValue(_ind);
                            most_projects.add(_map);
                        }

                        sortMapListByKeyValuePair(most_projects, "likes", true, false);
                        requireActivity().runOnUiThread(() -> {
                            BaseProjectAdapter mostAdapter = new BaseProjectAdapter(most_projects, requireActivity());
                            recyclerview_liked.setAdapter(mostAdapter);
                            hideViews(view.findViewById(R.id.linear_shimmer4));
                            showViews(view.findViewById(R.id.recyclerview_liked));
                        });
                    } catch (Exception ignored) {
                        // Handle exceptions
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle cancellations
                }
            });
        });
    }


    private boolean isNightMode() {
        int nightModeFlags = requireContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    public void transitionManager(final View view, final double duration) {
        LinearLayout viewGroup = (LinearLayout) view;

        AutoTransition autoTransition = new AutoTransition();
        autoTransition.setDuration((long) duration);
        autoTransition.setInterpolator(new DecelerateInterpolator());
        TransitionManager.beginDelayedTransition(viewGroup, autoTransition);
    }

    public void sortMapListByKeyValuePair(final ArrayList<HashMap<String, Object>> mapList, final String key, final boolean isNumeric, final boolean isAscending) {
        mapList.sort((map1, map2) -> {
            if (isNumeric) {
                int count1 = Integer.parseInt(String.valueOf(map1.get(key)));
                int count2 = Integer.parseInt(String.valueOf(map2.get(key)));
                if (isAscending) {
                    return Integer.compare(count1, count2);
                } else {
                    return Integer.compare(count2, count1);
                }
            } else {
                String value1 = String.valueOf(map1.get(key));
                String value2 = String.valueOf(map2.get(key));
                if (isAscending) {
                    return value1.compareTo(value2);
                } else {
                    return value2.compareTo(value1);
                }
            }
        });
    }

    private void delayTask(Runnable task, long delayMillis) {
        Handler handler = new Handler();
        handler.postDelayed(task, delayMillis);
    }

    private void delayTask(Runnable task) {
        delayTask(task, 200);
    }

    private void hideViews(View... views) {
        for (View view : views) {
            view.setVisibility(View.GONE);
        }
    }

    private void showViews(View... views) {
        for (View view : views) {
            view.setVisibility(View.VISIBLE);
        }
    }
}