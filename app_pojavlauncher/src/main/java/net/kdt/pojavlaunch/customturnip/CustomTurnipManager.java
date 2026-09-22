package net.kdt.pojavlaunch.customturnip;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CustomTurnipManager {
    private static final String TAG = "CustomTurnipManager";

    public static final String DRIVER_ID_DEFAULT = "default";
    public static final String DRIVER_ID_SYSTEM = "system";
    public static final String LIB_TURNIP_NAME = "libvulkan_freedreno.so";

    public static class TurnipDriver {
        public final String id;
        public final String displayName;
        public final String description;
        public final File driverFile;
        public final boolean isCustom;

        public TurnipDriver(String id, String displayName, String description, File driverFile, boolean isCustom) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
            this.driverFile = driverFile;
            this.isCustom = isCustom;
        }
    }

    public static File getDriversDirectory() {
        File dir = new File(Tools.TURNIP_DRIVERS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static List<TurnipDriver> getInstalledDrivers(Context context) {
        List<TurnipDriver> list = new ArrayList<>();

        // 1. Built-in Turnip (Default)
        list.add(new TurnipDriver(
                DRIVER_ID_DEFAULT,
                context.getString(R.string.turnip_driver_default_title),
                context.getString(R.string.turnip_driver_default_desc),
                null,
                false
        ));

        // 2. System Vulkan Driver
        list.add(new TurnipDriver(
                DRIVER_ID_SYSTEM,
                context.getString(R.string.turnip_driver_system_title),
                context.getString(R.string.turnip_driver_system_desc),
                null,
                false
        ));

        // 3. User Installed Custom Drivers
        File root = getDriversDirectory();
        File[] subdirs = root.listFiles(File::isDirectory);
        if (subdirs != null) {
            for (File dir : subdirs) {
                File soFile = findDriverSoInDir(dir);
                if (soFile != null && soFile.exists()) {
                    long sizeKb = soFile.length() / 1024;
                    list.add(new TurnipDriver(
                            dir.getName(),
                            dir.getName(),
                            sizeKb + " KB (" + soFile.getName() + ")",
                            soFile,
                            true
                    ));
                }
            }
        }

        return list;
    }

    public static File findDriverSoInDir(File dir) {
        File directSo = new File(dir, LIB_TURNIP_NAME);
        if (directSo.exists()) return directSo;

        File adrenoSo = new File(dir, "vulkan.adreno.so");
        if (adrenoSo.exists()) return adrenoSo;

        // Recursive search 1 level down
        File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    File nested = findDriverSoInDir(child);
                    if (nested != null) return nested;
                } else if (child.getName().endsWith(".so") && child.getName().contains("freedreno")) {
                    return child;
                }
            }
        }
        return null;
    }

    public static File getDriverFile(String driverId) {
        if (DRIVER_ID_DEFAULT.equals(driverId) || DRIVER_ID_SYSTEM.equals(driverId) || driverId == null) {
            return null;
        }
        File dir = new File(getDriversDirectory(), driverId);
        return findDriverSoInDir(dir);
    }

    public static String getActiveDriverId() {
        return LauncherPreferences.PREF_CUSTOM_TURNIP_DRIVER;
    }

    public static void setActiveDriver(String driverId) {
        if (driverId == null || driverId.isEmpty()) {
            driverId = DRIVER_ID_DEFAULT;
        }
        LauncherPreferences.PREF_CUSTOM_TURNIP_DRIVER = driverId;
        boolean preferSystem = DRIVER_ID_SYSTEM.equals(driverId);
        LauncherPreferences.PREF_ZINK_PREFER_SYSTEM_DRIVER = preferSystem;

        LauncherPreferences.DEFAULT_PREF.edit()
                .putString("customTurnipDriver", driverId)
                .putBoolean("zinkPreferSystemDriver", preferSystem)
                .apply();
    }

    public static boolean deleteDriver(String driverId) {
        if (DRIVER_ID_DEFAULT.equals(driverId) || DRIVER_ID_SYSTEM.equals(driverId)) {
            return false;
        }
        File dir = new File(getDriversDirectory(), driverId);
        boolean deleted = false;
        if (dir.exists()) {
            try {
                FileUtils.deleteDirectory(dir);
                deleted = true;
            } catch (IOException e) {
                Log.e(TAG, "Failed to delete driver directory: " + dir.getAbsolutePath(), e);
            }
        }
        if (driverId.equals(getActiveDriverId())) {
            setActiveDriver(DRIVER_ID_DEFAULT);
        }
        return deleted;
    }

    public static String installDriver(Context context, Uri uri) throws IOException {
        String fileName = Tools.getFileName(context, uri);
        if (fileName == null || fileName.isEmpty()) {
            fileName = "custom_turnip_" + System.currentTimeMillis();
        }

        String driverName = fileName;
        if (driverName.toLowerCase().endsWith(".zip")) {
            driverName = driverName.substring(0, driverName.length() - 4);
        } else if (driverName.toLowerCase().endsWith(".so")) {
            driverName = driverName.substring(0, driverName.length() - 3);
        }
        driverName = driverName.replaceAll("[^a-zA-Z0-9._-]", "_");

        File destDir = new File(getDriversDirectory(), driverName);
        if (destDir.exists()) {
            FileUtils.deleteDirectory(destDir);
        }
        destDir.mkdirs();

        try (InputStream rawIn = context.getContentResolver().openInputStream(uri)) {
            if (rawIn == null) {
                throw new IOException("Cannot open input stream for " + uri);
            }
            BufferedInputStream bis = new BufferedInputStream(rawIn);

            if (fileName.toLowerCase().endsWith(".zip")) {
                extractZip(bis, destDir);
            } else {
                // Raw .so file
                File destFile = new File(destDir, LIB_TURNIP_NAME);
                try (java.io.OutputStream bos = new java.io.BufferedOutputStream(new FileOutputStream(destFile), 65536)) {
                    IOUtils.copy(bis, bos);
                }
            }
        }

        // Validate that a driver shared library exists
        File soFile = findDriverSoInDir(destDir);
        if (soFile == null || !soFile.exists()) {
            FileUtils.deleteDirectory(destDir);
            throw new IOException("No valid Vulkan/Turnip driver (.so) found in the selected file.");
        }

        // If found library is not libvulkan_freedreno.so in destDir root, ensure a copy/link exists
        File primarySo = new File(destDir, LIB_TURNIP_NAME);
        if (!primarySo.exists() && !soFile.equals(primarySo)) {
            try {
                FileUtils.copyFile(soFile, primarySo);
            } catch (IOException e) {
                Log.w(TAG, "Could not copy " + soFile.getName() + " to " + LIB_TURNIP_NAME, e);
            }
        }

        // Make executable
        setExecutableRecursively(destDir);

        return driverName;
    }

    public static void installTurnipDriverFromUri(Context context, Uri uri, Runnable onComplete) {
        PojavApplication.sExecutorService.execute(() -> {
            try {
                String installedName = installDriver(context, uri);
                Tools.runOnUiThread(() -> {
                    Toast.makeText(context, context.getString(R.string.turnip_driver_installed_success, installedName), Toast.LENGTH_LONG).show();
                    if (onComplete != null) onComplete.run();
                });
            } catch (Exception e) {
                Tools.showError(context, e);
            }
        });
    }

    private static void extractZip(InputStream is, File targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is, 65536))) {
            ZipEntry entry;
            byte[] buffer = new byte[65536];
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.contains("..")) {
                    zis.closeEntry();
                    continue; // Path traversal security protection
                }
                File outFile = new File(targetDir, name);
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    File parent = outFile.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    try (java.io.OutputStream bos = new java.io.BufferedOutputStream(new FileOutputStream(outFile), 65536)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            bos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    private static void setExecutableRecursively(File file) {
        if (file.isDirectory()) {
            file.setExecutable(true, false);
            file.setReadable(true, false);
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    setExecutableRecursively(f);
                }
            }
        } else {
            file.setReadable(true, false);
            if (file.getName().endsWith(".so")) {
                file.setExecutable(true, false);
            }
        }
    }
}
