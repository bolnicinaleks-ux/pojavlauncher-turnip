package net.kdt.pojavlaunch.modloaders;

import com.kdt.mcgui.ProgressLayout;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.utils.DownloadUtils;
import net.kdt.pojavlaunch.utils.FileUtils;
import net.kdt.pojavlaunch.utils.ZipUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipFile;

public class NeoForgeDownloadTask implements Runnable, Tools.DownloaderFeedback {
    private String mDownloadUrl;
    private String mFullVersion;
    private String mLoaderVersion;
    private String mGameVersion;
    private final ModloaderDownloadListener mListener;

    public NeoForgeDownloadTask(ModloaderDownloadListener listener, String neoForgeVersion) {
        this.mListener = listener;
        this.mDownloadUrl = NeoForgeUtils.getInstallerUrl(neoForgeVersion);
        this.mFullVersion = neoForgeVersion;
    }

    public NeoForgeDownloadTask(ModloaderDownloadListener listener, String gameVersion, String loaderVersion) {
        this.mListener = listener;
        this.mLoaderVersion = loaderVersion;
        this.mGameVersion = gameVersion;
    }

    @Override
    public void run() {
        if (determineDownloadUrl()) {
            installNeoForge();
        }
        ProgressLayout.clearProgress(ProgressLayout.INSTALL_MODPACK);
    }

    @Override
    public void updateProgress(int curr, int max) {
        int progress100 = (int) (((float) curr / (float) max) * 100f);
        ProgressKeeper.submitProgress(ProgressLayout.INSTALL_MODPACK, progress100, R.string.forge_dl_progress, "NeoForge " + mFullVersion);
    }

    private void installNeoForge() {
        ProgressKeeper.submitProgress(ProgressLayout.INSTALL_MODPACK, 0, R.string.forge_dl_progress, "NeoForge " + mFullVersion);
        try {
            File installerJar = new File(Tools.DIR_CACHE, "neoforge-installer.jar");
            byte[] buffer = new byte[8192];
            DownloadUtils.downloadFileMonitored(mDownloadUrl, installerJar, buffer, this);

            // Extract version.json directly from installer jar into versions/neoforge-VERSION/neoforge-VERSION.json
            try (ZipFile zipFile = new ZipFile(installerJar)) {
                String versionJsonContent = Tools.read(ZipUtils.getEntryStream(zipFile, "version.json"));
                String versionId = "neoforge-" + mFullVersion;
                File versionDir = new File(Tools.DIR_HOME_VERSION, versionId);
                File versionJsonFile = new File(versionDir, versionId + ".json");
                FileUtils.ensureDirectory(versionDir);
                Tools.write(versionJsonFile.getAbsolutePath(), versionJsonContent);
            }

            mListener.onDownloadFinished(installerJar);
        } catch (FileNotFoundException e) {
            mListener.onDataNotAvailable();
        } catch (IOException e) {
            mListener.onDownloadError(e);
        }
    }

    public boolean determineDownloadUrl() {
        if (mDownloadUrl != null && mFullVersion != null) return true;
        ProgressKeeper.submitProgress(ProgressLayout.INSTALL_MODPACK, 0, R.string.forge_dl_searching);
        try {
            if (!findVersion()) {
                mListener.onDataNotAvailable();
                return false;
            }
        } catch (IOException e) {
            mListener.onDownloadError(e);
            return false;
        }
        return true;
    }

    public boolean findVersion() throws IOException {
        if (mLoaderVersion != null && !mLoaderVersion.isEmpty()) {
            mFullVersion = mLoaderVersion;
            mDownloadUrl = NeoForgeUtils.getInstallerUrl(mFullVersion);
            return true;
        }
        List<String> neoForgeVersions = NeoForgeUtils.downloadNeoForgeVersions();
        if (neoForgeVersions == null) return false;
        for (String versionName : neoForgeVersions) {
            if (mLoaderVersion != null && versionName.startsWith(mLoaderVersion)) {
                mFullVersion = versionName;
                mDownloadUrl = NeoForgeUtils.getInstallerUrl(mFullVersion);
                return true;
            }
        }
        return false;
    }
}
