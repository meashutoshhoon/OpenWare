package in.afi.codekosh.pages;

import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class PageContainer {
    private final AppCompatActivity context;
    private View view;

    public PageContainer(AppCompatActivity activity) {
        this.context = activity;
    }

    public PageContainer attach(View view) {
        this.view = view;
        return this;
    }

    public void addPage(Page page) {
        FragmentManager fragmentManager = context.getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(view.getId(), page);
        transaction.commit();
    }
}
