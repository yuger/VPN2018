package com.wxy.vpn2018;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * 清除缓存 数据库等
 * Created by Administrator on 2016/10/10.
 */

public class DataCleanManager {
    /**
     * 清除本应用内部缓存(/data/data/com.xxx.xxx/cache)
     */
    public static void cleanCache(Context context) {
        deleteFilesByDirectory(context.getCacheDir()); // InternalCache
        cleanExternalCache(context);// 同时清除外部cache下的内容(/mnt/sdcard/android/data/com.xxx.xxx/cache)
    }

    /**
     * 清除本应用所有数据库(/data/data/com.xxx.xxx/databases)
     */
//    private static void cleanDatabases(Context context) {
//        deleteFilesByDirectory(new File("/data/data/" + context.getPackageName() + "/databases"));
//    }
    private static void deleteDatabases(Context context) {
        for (String database : context.databaseList()) {
            context.deleteDatabase(database);
        }
    }

    /**
     * 清除本应用SharedPreference(/data/data/com.xxx.xxx/shared_prefs) android N:/data/user/0/package_name/
     */
    private static void cleanSharedPreference(Context context) {
        deleteFilesByDirectory(new File(/*"/data/data/" + context.getPackageName()*/ context.getFilesDir().getParent() + "/shared_prefs"));
    }

    /**
     * 按名字清除本应用数据库
     */
//    public static void cleanDatabaseByName(Context context, String dbName) {
//        context.deleteDatabase(dbName);
//    }

    /**
     * 清除/data/data/com.xxx.xxx/files下的内容
     */
    private static void cleanFiles(Context context) {
        deleteFilesByDirectory(context.getFilesDir());
    }

    /**
     * * 清除外部cache下的内容(/mnt/sdcard/android/data/com.xxx.xxx/cache)
     */
    private static void cleanExternalCache(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            deleteFilesByDirectory(context.getExternalCacheDir());
        }
    }

    /**
     * 清除自定义路径下的文件，使用需小心，请不要误删。而且只支持目录下的文件删除
     */
//    public static void cleanCustomCache(String filePath) {
//        deleteFilesByDirectory(new File(filePath));
//    }

    /**
     * 清除本应用所有的数据
     */
    public static void cleanApplicationData(Context context/*, String... filepath*/) {
        cleanCache(context);
//        cleanExternalCache(context);
//        cleanDatabases(context);
        deleteDatabases(context);
        cleanSharedPreference(context);
        cleanFiles(context);
//        for (String filePath : filepath) {
//            cleanCustomCache(filePath);
//        }
    }

    /**
     * 删除方法 这里只会删除某个文件夹下的文件，如果传入的directory是个文件，将不做处理
     */
    private static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
//                item.deleteOnExit();
            }
        }
    }

    // 获取文件/文件夹大小
    //Context.getExternalFilesDir() --> SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
    //Context.getExternalCacheDir() --> SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据
    public static float getFolderSize(File file) {
        float size = 0;
        try {
            File[] fileList = file.listFiles();
            for (File aFileList : fileList) {
                // 如果下面还有文件
                if (aFileList.isDirectory()) {
                    size = size + getFolderSize(aFileList);
                } else {
                    size = size + aFileList.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return size;
    }
}
