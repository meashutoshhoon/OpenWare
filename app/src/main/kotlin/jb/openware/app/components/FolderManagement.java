package in.afi.codekosh.components;

import android.os.Environment;

public class FolderManagement {

    private static final String[] FOLDER_NAMES = {
            "Download/CodeKosh",
            "Documents/CodeKosh",
            "Download/CodeKosh/Sketchware",
            "Download/CodeKosh/Sketchware/temp",
            "Download/CodeKosh/Sketchware/.temp",
            "Download/CodeKosh/Sketchware Pro",
            "Download/CodeKosh/Android Studio",
            "Download/CodeKosh/HTML",
            "Documents/CodeKosh/Sketchware Projects",
            "Documents/CodeKosh/Sketchware Projects/Export"
    };

    public FolderManagement() {
    }

    public void makeFolders() {
        for (String folderName : FOLDER_NAMES) {
            String folderPath = Environment.getExternalStorageDirectory() + "/" + folderName;
            if (!FileUtil.isExistFile(folderPath)) {
                FileUtil.makeDir(folderPath);
            }
        }
    }
}
