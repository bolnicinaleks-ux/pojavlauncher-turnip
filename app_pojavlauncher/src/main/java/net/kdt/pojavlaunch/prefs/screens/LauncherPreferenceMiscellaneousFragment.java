package net.kdt.pojavlaunch.prefs.screens;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.preference.Preference;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;
import net.kdt.pojavlaunch.customturnip.CustomTurnipManager;
import net.kdt.pojavlaunch.customturnip.TurnipConfigDialog;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.utils.GLInfoUtils;

public class LauncherPreferenceMiscellaneousFragment extends LauncherPreferenceFragment {
    private TurnipConfigDialog mTurnipDialog;
    private Preference mManageDriversPref;

    private final ActivityResultLauncher<Object> mTurnipInstallLauncher =
            registerForActivityResult(new OpenDocumentWithExtension("*"), (data) -> {
                if (data != null && getContext() != null) {
                    CustomTurnipManager.installTurnipDriverFromUri(getContext(), data, () -> {
                        if (mTurnipDialog != null) mTurnipDialog.refresh();
                        updateDriverSummary();
                    });
                }
            });

    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_misc);

        Preference driverPreference = requirePreference("zinkPreferSystemDriver");
        mManageDriversPref = requirePreference("manageCustomTurnipDrivers");

        PackageManager packageManager = driverPreference.getContext().getPackageManager();
        boolean supportsTurnip = Tools.checkVulkanSupport(packageManager) && GLInfoUtils.getGlInfo().isAdreno();

        driverPreference.setVisible(supportsTurnip);
        mManageDriversPref.setVisible(supportsTurnip);

        mManageDriversPref.setOnPreferenceClickListener(preference -> {
            openTurnipConfigDialog();
            return true;
        });

        updateDriverSummary();
    }

    private void updateDriverSummary() {
        if (mManageDriversPref == null || getContext() == null) return;
        String active = CustomTurnipManager.getActiveDriverId();
        if (CustomTurnipManager.DRIVER_ID_DEFAULT.equals(active)) {
            mManageDriversPref.setSummary(getString(R.string.turnip_driver_current, getString(R.string.turnip_driver_default_title)));
        } else if (CustomTurnipManager.DRIVER_ID_SYSTEM.equals(active)) {
            mManageDriversPref.setSummary(getString(R.string.turnip_driver_current, getString(R.string.turnip_driver_system_title)));
        } else {
            mManageDriversPref.setSummary(getString(R.string.turnip_driver_current, active));
        }
    }

    private void openTurnipConfigDialog() {
        if (mTurnipDialog == null) {
            mTurnipDialog = new TurnipConfigDialog();
            mTurnipDialog.prepare(requireContext(), mTurnipInstallLauncher);
        }
        mTurnipDialog.show();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences p, String s) {
        super.onSharedPreferenceChanged(p, s);
        if ("customTurnipDriver".equals(s) || "zinkPreferSystemDriver".equals(s)) {
            updateDriverSummary();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDriverSummary();
    }
}
