package jb.openware.app.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.MenuRes;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.PopupMenu;

public class MenuCreator {

    private final PopupMenu popupMenu;

    public MenuCreator(Context context, View anchorView) {
        popupMenu = new PopupMenu(context, anchorView);
        if(popupMenu.getMenu() instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) popupMenu.getMenu();
            //noinspection RestrictedApi
            m.setOptionalIconsVisible(true);
        }
    }

    public void inflate(@MenuRes int menuRes) {
        popupMenu.getMenuInflater().inflate(menuRes, popupMenu.getMenu());
    }

    public void addItem(int groupId, int itemId, int order, CharSequence title) {
        popupMenu.getMenu().add(groupId, itemId, order, title);

    }

    public void setIcon(int itemId, int iconResId) {
        MenuItem menuItem = popupMenu.getMenu().findItem(itemId);
        if (menuItem != null) {
            menuItem.setIcon(iconResId);
        }

    }

    public void setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener listener) {
        popupMenu.setOnMenuItemClickListener(listener);

    }

    public void show() {
        popupMenu.show();

    }
}