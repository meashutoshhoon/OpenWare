package in.afi.codekosh.components;

import android.net.Uri;
import android.os.Environment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SketchwareUtils {
    private final ArrayList<String> temp_str1 = new ArrayList<>();
    private final ArrayList<String> copy_list = new ArrayList<>();
    private final String pathSketchware = FileUtil.getExternalStorageDir().concat("/.sketchware/");
    private final String pathCodeKosh = Environment.getExternalStorageDirectory() + "/Documents/CodeKosh/Sketchware Projects/Export";
    private final ArrayList<String> temp = new ArrayList<>();
    String downloadsFolder = Environment.getExternalStorageDirectory() + "/Download/";
    private final String pathHideCodeKosh = downloadsFolder + "CodeKosh/Sketchware/.temp";
    private String new_id = "";
    private String temp_decrypted = "";

    private static void unzip(String zipFilePath, String destDir) {
        File destDirectory = new File(destDir);
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                String entryName = entry.getName();
                File entryFile = new File(destDirectory, entryName);

                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    extractFile(zipIn, entryFile);
                }
            }
        } catch (Exception ignored) {

        }
    }

    private static void extractFile(ZipInputStream zipIn, File entryFile) throws IOException {
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(entryFile))) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = zipIn.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    public ArrayList<HashMap<String, Object>> loadProjects() {
        ArrayList<HashMap<String, Object>> tempListMap = new ArrayList<>();
        String sketchwareDir = FileUtil.getExternalStorageDir().concat("/.sketchware/mysc/list/");
        FileUtil.listDir(sketchwareDir, temp_str1);
        temp_str1.sort(String.CASE_INSENSITIVE_ORDER);

        for (String filePath : temp_str1) {
            String projectDir = sketchwareDir.concat(Uri.parse(filePath).getLastPathSegment().concat("/project"));

            if (FileUtil.isExistFile(projectDir)) {
                try {
                    String encryptionKey = "sketchwaresecure";
                    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    byte[] keyBytes = encryptionKey.getBytes();
                    cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new IvParameterSpec(keyBytes));

                    RandomAccessFile randomAccessFile = new RandomAccessFile(projectDir, "r");
                    byte[] fileBytes = new byte[(int) randomAccessFile.length()];
                    randomAccessFile.readFully(fileBytes);

                    String decryptedData = new String(cipher.doFinal(fileBytes));
                    HashMap<String, Object> temp_map = new Gson().fromJson(decryptedData, new TypeToken<HashMap<String, Object>>() {
                    }.getType());
                    tempListMap.add(temp_map);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Collections.reverse(tempListMap);
        return tempListMap;

    }

    public void exportProject(String name, final int position, final ArrayList<HashMap<String, Object>> listMap) {
        FileUtil.makeDir(pathHideCodeKosh.concat("/data/"));
        FileUtil.makeDir(pathHideCodeKosh.concat("/list/"));
        FileUtil.makeDir(pathHideCodeKosh.concat("/fonts/"));
        FileUtil.makeDir(pathHideCodeKosh.concat("/icons/"));
        FileUtil.makeDir(pathHideCodeKosh.concat("/images/"));
        FileUtil.makeDir(pathHideCodeKosh.concat("/sounds/"));
        _Copy(pathSketchware.concat("data/".concat(String.valueOf(listMap.get(position).get("sc_id")))), pathHideCodeKosh.concat("/data/"));
        _Copy(pathSketchware.concat("mysc/list/".concat(String.valueOf(listMap.get(position).get("sc_id")))), pathHideCodeKosh.concat("/list/"));
        _Copy(pathSketchware.concat("resources/fonts/".concat(String.valueOf(listMap.get(position).get("sc_id")))), pathHideCodeKosh.concat("/fonts/"));
        _Copy(pathSketchware.concat("resources/icons/".concat(String.valueOf(listMap.get(position).get("sc_id")))), pathHideCodeKosh.concat("/icons/"));
        _Copy(pathSketchware.concat("resources/images/".concat(String.valueOf(listMap.get(position).get("sc_id")))), pathHideCodeKosh.concat("/images/"));
        _Copy(pathSketchware.concat("resources/sounds/".concat(String.valueOf(listMap.get(position).get("sc_id")))), pathHideCodeKosh.concat("/sounds/"));
        _zip(pathHideCodeKosh, pathCodeKosh.concat("/" + name + ".zip"));
    }

    public void exportProject2(String name, final int position, final ArrayList<HashMap<String, Object>> listMap) {
        FileUtil.makeDir(pathHideCodeKosh.concat("/data/"));
        FileUtil.makeDir(pathHideCodeKosh.concat("/list/"));
        FileUtil.makeDir(pathHideCodeKosh.concat("/fonts/"));
        FileUtil.makeDir(pathHideCodeKosh.concat("/icons/"));
        FileUtil.makeDir(pathHideCodeKosh.concat("/images/"));
        FileUtil.makeDir(pathHideCodeKosh.concat("/sounds/"));
        _Copy(pathSketchware.concat("data/".concat(String.valueOf(listMap.get(position).get("sc_id")))), pathHideCodeKosh.concat("/data/"));
        _Copy(pathSketchware.concat("mysc/list/".concat(String.valueOf(listMap.get(position).get("sc_id")))), pathHideCodeKosh.concat("/list/"));
        _Copy(pathSketchware.concat("resources/fonts/".concat(String.valueOf(listMap.get(position).get("sc_id")))), pathHideCodeKosh.concat("/fonts/"));
        _Copy(pathSketchware.concat("resources/icons/".concat(String.valueOf(listMap.get(position).get("sc_id")))), pathHideCodeKosh.concat("/icons/"));
        _Copy(pathSketchware.concat("resources/images/".concat(String.valueOf(listMap.get(position).get("sc_id")))), pathHideCodeKosh.concat("/images/"));
        _Copy(pathSketchware.concat("resources/sounds/".concat(String.valueOf(listMap.get(position).get("sc_id")))), pathHideCodeKosh.concat("/sounds/"));
        FileUtil.writeFile(pathHideCodeKosh + "/index.json", "");
        FileUtil.encrypt(pathHideCodeKosh + "/index.json", "Zx7Rt9Lp2QwFy6XsGmHdJvP1uKo4qEzNxYfAaDgBhCk5iMw8jLoTp2Kw9Nu0rFzGhYvC");
        _zip(pathHideCodeKosh, pathCodeKosh.concat("/" + name + ".ckafi"));
    }


    public void importProject(String path) {
        unzip(path, pathHideCodeKosh);
        newIdGeneration();
        FileUtil.makeDir(pathSketchware.concat("data/".concat(new_id)));
        FileUtil.makeDir(pathSketchware.concat("mysc/list/".concat(new_id)));
        FileUtil.makeDir(pathSketchware.concat("resources/fonts/".concat(new_id)));
        FileUtil.makeDir(pathSketchware.concat("resources/icons/".concat(new_id)));
        FileUtil.makeDir(pathSketchware.concat("resources/images/".concat(new_id)));
        FileUtil.makeDir(pathSketchware.concat("resources/sounds/".concat(new_id)));
        _Decrypt(pathHideCodeKosh.concat("/.temp/list/project"));
        HashMap<String, Object> decrypt_map = new Gson().fromJson(temp_decrypted, new TypeToken<HashMap<String, Object>>() {
        }.getType());
        decrypt_map.put("sc_id", new_id);
        temp_decrypted = new Gson().toJson(decrypt_map);
        _Encrypt(pathHideCodeKosh.concat("/.temp/list/project"));
        _Copy(pathHideCodeKosh.concat("/.temp/data"), pathSketchware.concat("data/".concat(new_id)));
        _Copy(pathHideCodeKosh.concat("/.temp/list/"), pathSketchware.concat("mysc/list/".concat(new_id)));
        _Copy(pathHideCodeKosh.concat("/.temp/fonts/"), pathSketchware.concat("resources/fonts/".concat(new_id)));
        _Copy(pathHideCodeKosh.concat("/.temp/icons/"), pathSketchware.concat("resources/icons/".concat(new_id)));
        _Copy(pathHideCodeKosh.concat("/.temp/images/"), pathSketchware.concat("resources/images/".concat(new_id)));
        _Copy(pathHideCodeKosh.concat("/.temp/sounds/"), pathSketchware.concat("resources/sounds/".concat(new_id)));
    }

    public String importProject2(String path) {
        unzip(path, pathHideCodeKosh);
        if (FileUtil.isExistFile(pathHideCodeKosh + "/.temp/index.json")) {
            String s = FileUtil.decrypt(pathHideCodeKosh + "/.temp/index.json");
            if (s.equals("Zx7Rt9Lp2QwFy6XsGmHdJvP1uKo4qEzNxYfAaDgBhCk5iMw8jLoTp2Kw9Nu0rFzGhYvC")) {
                newIdGeneration();
                FileUtil.makeDir(pathSketchware.concat("data/".concat(new_id)));
                FileUtil.makeDir(pathSketchware.concat("mysc/list/".concat(new_id)));
                FileUtil.makeDir(pathSketchware.concat("resources/fonts/".concat(new_id)));
                FileUtil.makeDir(pathSketchware.concat("resources/icons/".concat(new_id)));
                FileUtil.makeDir(pathSketchware.concat("resources/images/".concat(new_id)));
                FileUtil.makeDir(pathSketchware.concat("resources/sounds/".concat(new_id)));
                _Decrypt(pathHideCodeKosh.concat("/.temp/list/project"));
                HashMap<String, Object> decrypt_map = new Gson().fromJson(temp_decrypted, new TypeToken<HashMap<String, Object>>() {
                }.getType());
                decrypt_map.put("sc_id", new_id);
                temp_decrypted = new Gson().toJson(decrypt_map);
                _Encrypt(pathHideCodeKosh.concat("/.temp/list/project"));
                _Copy(pathHideCodeKosh.concat("/.temp/data"), pathSketchware.concat("data/".concat(new_id)));
                _Copy(pathHideCodeKosh.concat("/.temp/list/"), pathSketchware.concat("mysc/list/".concat(new_id)));
                _Copy(pathHideCodeKosh.concat("/.temp/fonts/"), pathSketchware.concat("resources/fonts/".concat(new_id)));
                _Copy(pathHideCodeKosh.concat("/.temp/icons/"), pathSketchware.concat("resources/icons/".concat(new_id)));
                _Copy(pathHideCodeKosh.concat("/.temp/images/"), pathSketchware.concat("resources/images/".concat(new_id)));
                _Copy(pathHideCodeKosh.concat("/.temp/sounds/"), pathSketchware.concat("resources/sounds/".concat(new_id)));
                return "s";
            } else {
                return "ncf";
            }
        } else {
            return "fne";
        }

    }

    private void _Decrypt(final String _path) {
        try {
            Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] bytes = "sketchwaresecure".getBytes();
            instance.init(2, new SecretKeySpec(bytes, "AES"), new IvParameterSpec(bytes));
            RandomAccessFile randomAccessFile = new RandomAccessFile(_path, "r");
            byte[] bArr = new byte[((int) randomAccessFile.length())];
            randomAccessFile.readFully(bArr);
            temp_decrypted = new String(instance.doFinal(bArr));
        } catch (Exception ignored) {
        }
    }

    private void _Encrypt(final String _path) {
        try {
            Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] bytes = "sketchwaresecure".getBytes();
            instance.init(1, new SecretKeySpec(bytes, "AES"), new IvParameterSpec(bytes));
            new RandomAccessFile(_path, "rw").write(instance.doFinal(temp_decrypted.getBytes()));
        } catch (Exception ignored) {
        }
    }

    public void deleteTemp2() {
        FileUtil.deleteFile(pathHideCodeKosh.concat("/.temp"));
    }

    public void deleteTemp() {
        FileUtil.deleteFile(pathHideCodeKosh.concat("/data"));
        FileUtil.deleteFile(pathHideCodeKosh.concat("/list"));
        FileUtil.deleteFile(pathHideCodeKosh.concat("/fonts"));
        FileUtil.deleteFile(pathHideCodeKosh.concat("/icons"));
        FileUtil.deleteFile(pathHideCodeKosh.concat("/images"));
        FileUtil.deleteFile(pathHideCodeKosh.concat("/sounds"));
        FileUtil.deleteFile(pathHideCodeKosh.concat("/index.json"));
    }

    private void newIdGeneration() {
        FileUtil.listDir(FileUtil.getExternalStorageDir() + "/.sketchware/mysc/list", temp);
        temp.sort(String.CASE_INSENSITIVE_ORDER);
        new_id = temp.isEmpty() ? "601" : String.valueOf((long) (Double.parseDouble(new File(temp.get(temp.size() - 1)).getName()) + 1));
    }

    private void _Copy(final String _F, final String _T) {
        copy_list.clear();
        FileUtil.listDir(_F.concat("/"), copy_list);

        for (String filePath : copy_list) {
            if (FileUtil.isFile(filePath)) {
                String fileName = new File(filePath).getName();
                String destinationPath = _T.concat("/").concat(fileName);
                FileUtil.copyFile(filePath, destinationPath);
            }
        }
    }

    private void _zip(final String _source, final String _destination) {
        try (ZipOutputStream os = new ZipOutputStream(new FileOutputStream(_destination))) {
            zip(os, _source, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void zip(ZipOutputStream os, String filePath, String name) throws IOException {
        File file = new File(filePath);
        ZipEntry entry = new ZipEntry((name != null ? name + java.io.File.separator : "") + file.getName() + (file.isDirectory() ? java.io.File.separator : ""));
        os.putNextEntry(entry);

        if (file.isFile()) {
            InputStream is = new FileInputStream(file);
            int size = is.available();
            byte[] buff = new byte[size];
            int len = is.read(buff);
            os.write(buff, 0, len);
            return;
        }

        File[] fileArr = file.listFiles();
        for (File subFile : fileArr) {
            zip(os, subFile.getAbsolutePath(), entry.getName());
        }
    }

}
