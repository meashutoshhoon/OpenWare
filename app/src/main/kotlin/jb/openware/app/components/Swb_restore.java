package in.afi.codekosh.components;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import in.afi.codekosh.BuildConfig;

public class Swb_restore {
    private static final ArrayList<String> str2 = new ArrayList<>();
    private static final ArrayList<String> str4 = new ArrayList<>();
    private static String tempN = "";
    private static String newId = "";
    private static String path = "";
    private static String s1 = "";
    private static String s2 = "";
    private final Context context;

    public Swb_restore(Context context) {
        this.context = context;
    }

    public static void _copy_data() {
        s1 = path.concat("data/");
        s2 = FileUtil.getExternalStorageDir().concat("/.sketchware/data/".concat(newId.concat("/")));
        File var0 = new File(s1);
        File var1 = new File(s2);


        copyDirectory(var0, var1);


        if (localLibrary()) {
            _copy_local_library();
        } else {
            _copy_resources();
        }

    }

    public static void copyDirectory(File oldPath, File newFile) {
        File[] files = oldPath.listFiles();
        if (!newFile.exists()) {
            newFile.mkdirs();
        }
        for (File file : files) {
            if (file.isFile()) {
                FileUtil.copyFile(file.getPath(), newFile.getPath() + "/" + file.getName());
            } else if (file.isDirectory()) {
                File f = new File(newFile + "/" + file.getName());
                copyDirectory(file, f);
            }
        }
    }

    public static void _copy_local_library() {
        FileUtil.listDir(path.concat("local_libs/"), str2);
        FileUtil.listDir(FileUtil.getExternalStorageDir().concat("/.sketchware/local_libs/libs/"), str4);
        double a = 0.0D;

        for (int var0 = 0; var0 < str2.size(); ++var0) {
            if (!Objects.equals(Uri.parse((String) str2.get((int) a)).getLastPathSegment(), (new Gson()).toJson(str4))) {
                s1 = (String) str2.get((int) a);
                s2 = FileUtil.getExternalStorageDir().concat("/.sketchware/local_libs/libs/".concat(Uri.parse((String) str2.get((int) a)).getLastPathSegment().concat("/")));
                File var1 = new File(s1);
                File var2 = new File(s2);


                copyDirectory(var1, var2);

            }

            ++a;
            if ((double) str2.size() == a) {
                _copy_resources();
            }
        }

    }

    public static void _copy_resources() {
        FileUtil.copyFile(path.concat("resources/icons/icon.png"), FileUtil.getExternalStorageDir().concat("/.sketchware/resources/icons/".concat(newId.concat("/icon.png"))));
        s1 = path.concat("resources/fonts/");
        s2 = FileUtil.getExternalStorageDir().concat("/.sketchware/resources/fonts/".concat(newId.concat("/")));
        File var0 = new File(s1);
        File var1 = new File(s2);

        copyDirectory(var0, var1);

        s1 = path.concat("resources/images/");
        s2 = FileUtil.getExternalStorageDir().concat("/.sketchware/resources/images/".concat(newId.concat("/")));
        var0 = new File(s1);
        var1 = new File(s2);


        copyDirectory(var0, var1);


        s1 = path.concat("resources/sounds/");
        s2 = FileUtil.getExternalStorageDir().concat("/.sketchware/resources/sounds/".concat(newId.concat("/")));
        var0 = new File(s1);
        var1 = new File(s2);


        copyDirectory(var0, var1);


        _copy_project_file();
    }

    public static void _copy_project_file() {
        String jsoj = _de("sketchwaresecure", path.concat("project"));
        HashMap<String, Object> map = (HashMap) (new Gson()).fromJson(jsoj, (new TypeToken<HashMap<String, Object>>() {
        }).getType());
        map.remove("sc_id");
        map.put("sc_id", newId);
        tempN = (new Gson()).toJson(map);
        FileUtil.makeDir(FileUtil.getExternalStorageDir().concat("/.sketchware/mysc/list/".concat(newId.concat("/"))));
        _en("sketchwaresecure", FileUtil.getExternalStorageDir().concat("/.sketchware/mysc/list/".concat(newId.concat("/project"))));
        if (_is_finish()) {
//            showMessage("SWB FILE SUCCESSFULLY RESTORED");
        }

    }

    public static boolean _is_finish() {
        boolean var0 = false;
        boolean var1 = var0;
        if (newId != null) {
            var1 = var0;
            if (FileUtil.isExistFile(FileUtil.getExternalStorageDir().concat("/.sketchware/mysc/list/".concat(newId.concat("/project"))))) {
                var1 = true;
            }
        }

        return var1;
    }

    public static String _de(String var0, String var1) {
        try {
            Cipher var2 = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] var3 = var0.getBytes();
            SecretKeySpec var6 = new SecretKeySpec(var3, "AES");
            IvParameterSpec var4 = new IvParameterSpec(var3);
            var2.init(2, var6, var4);
            RandomAccessFile var7 = new RandomAccessFile(var1, "r");
            byte[] var8 = new byte[(int) var7.length()];
            var7.readFully(var8);
            var0 = new String(var2.doFinal(var8));
        } catch (Exception var5) {
//            showMessage(var5.toString());
            var0 = "error occurred";
        }

        return var0;
    }

    public static void _en(String var0, String var1) {
        try {
            Cipher var2 = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] var3 = var0.getBytes();
            SecretKeySpec var6 = new SecretKeySpec(var3, "AES");
            IvParameterSpec var4 = new IvParameterSpec(var3);
            var2.init(1, var6, var4);
            FileOutputStream var7 = new FileOutputStream(var1);
            var7.write(var2.doFinal(tempN.getBytes()));
        } catch (Exception var5) {
//            showMessage(var5.toString());
        }

    }

    public static boolean localLibrary() {
        String localLibPath = path.concat("local_libs/");
        return FileUtil.isDirectory(localLibPath);
    }

    public static void unZip(String sourceZip, String destinationDir) throws IOException {
        File destDir = new File(destinationDir);
        destDir.mkdirs();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(sourceZip))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                if (zipEntry.isDirectory()) {
                    mkdirs(destDir, fileName);
                } else {
                    String parentDir = dirpart(fileName);
                    if (parentDir != null) {
                        mkdirs(destDir, parentDir);
                    }
                    extractFile(zis, destDir, fileName);
                }
                zipEntry = zis.getNextEntry();
            }
        }
    }

    public static String getId() {
        ArrayList<String> str = new ArrayList<>();
        FileUtil.listDir("/storage/emulated/0/.sketchware/mysc/list/", str);

        ArrayList<Integer> ids = new ArrayList<>();
        for (String filePath : str) {
            String fileName = Uri.parse(filePath).getLastPathSegment();
            if (fileName != null) {
                try {
                    int id = Integer.parseInt(fileName);
                    ids.add(id);
                } catch (NumberFormatException e) {
                    // Handle the case where the file name is not a valid integer
                }
            }
        }

        int maxId = ids.isEmpty() ? 0 : Collections.max(ids);
        return String.valueOf(maxId + 1);
    }

    private static void mkdirs(File parent, String child) {
        File directory = new File(parent, child);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    private static String dirpart(String filePath) {
        int lastIndex = filePath.lastIndexOf(File.separatorChar);
        if (lastIndex == -1) {
            return null;
        } else {
            return filePath.substring(0, lastIndex);
        }
    }

    private static void extractFile(ZipInputStream zipInputStream, File outputDirectory, String fileName) throws IOException {
        byte[] buffer = new byte[4096];
        File outputFile = new File(outputDirectory, fileName);

        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            int bytesRead;
            while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    public static void selectSWB(String path, Context context) {

        String zipPath = FileUtil.getPackageDataDir(context) + "/Swb_restore/zip/" + Uri.parse(path).getLastPathSegment();
        String extractedFolderPath = FileUtil.getPackageDataDir(context) + "/Swb_restore/";

        // Copy and rename the file
        FileUtil.copyFile(path, zipPath.replace(".swb", ".zip"));

        // Unzip the file
        try {
            unZip(zipPath.replace(".swb", ".zip"), extractedFolderPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Swb_restore.path = FileUtil.getPackageDataDir(context).concat("/Swb_restore/");

        // Process the extracted data
        newId = getId();
        _copy_data();


    }

    public void openSwb(String filePath) {
        File file = new File(filePath);
        Uri fileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        Intent myIntent = new Intent(Intent.ACTION_VIEW);
        myIntent.setData(fileUri);
        myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent j = Intent.createChooser(myIntent, "Choose an application to open with:");
        try {
            context.startActivity(j);
        } catch (ActivityNotFoundException ignored) {

        }

    }


}
