package lantian.nolitter;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SettingsFragment extends PreferenceFragment {
    public static SharedPreferences prefs;

    @Override
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        getPreferenceScreen().removePreference(findPreference("hidden"));
        findPreference("banned_ui").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ltChooseApp("banned", Constants.banned);
                return false;
            }
        });
        findPreference("forced_ui").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ltChooseApp("forced", Constants.forced);
                return false;
            }
        });
        findPreference("author").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://lantian.pub"));
                startActivity(browserIntent);
                return false;
            }
        });
        findPreference("source").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/xddxdd/lantian-nolitter"));
                startActivity(browserIntent);
                return false;
            }
        });
        findPreference("about_xinternalsd").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/pylerSM/XInternalSD"));
                startActivity(browserIntent);
                return false;
            }
        });
        findPreference("showicon").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean isVisible = (Boolean) newValue;
                if (isVisible) {
                    getActivity().getPackageManager().setComponentEnabledSetting(new ComponentName(getActivity(), MainActivity.class),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP);
                } else {
                    getActivity().getPackageManager().setComponentEnabledSetting(new ComponentName(getActivity(), MainActivity.class),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);
                }
                return true;
            }
        });
    }

    @Override
    public void onPause() {
        // From https://forum.xda-developers.com/xposed/how-to-save-load-module-settings-t3640881
        super.onPause();

        // Set preferences permissions to be world readable
        // Workaround for Android N and above since MODE_WORLD_READABLE will cause security exception and FC.
        final File dataDir = new File(getActivity().getApplicationInfo().dataDir);
        final File prefsDir = new File(dataDir, "shared_prefs");
        final File prefsFile = new File(prefsDir, getPreferenceManager().getSharedPreferencesName() + ".xml");

        if (prefsFile.exists()) {
            dataDir.setReadable(true, false);
            dataDir.setExecutable(true, false);

            prefsDir.setReadable(true, false);
            prefsDir.setExecutable(true, false);

            prefsFile.setReadable(true, false);
            prefsFile.setExecutable(true, false);
        }
    }

    public boolean ltChooseApp(final String key, final String defaults) {
        // Get app info
        final List<ApplicationInfo> appInfo;
        if (prefs.getBoolean("enable_system", false)) {
            appInfo = getActivity().getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        } else {
            appInfo = new ArrayList<>();
            for (ApplicationInfo appInfoSingle : getActivity().getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA)) {
                if ((appInfoSingle.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    appInfo.add(appInfoSingle);
                }
            }
        }
        Collections.sort(appInfo, new ApplicationInfo.DisplayNameComparator(getActivity().getPackageManager()));

        // Get current settings
        ArrayList<String> banned = new ArrayList<>(Arrays.asList(prefs.getString(key, defaults).split(",")));

        // Form arrays
        final String[] appTitles = new String[appInfo.size()];
        boolean[] appCared = new boolean[appInfo.size()];
        for (int i = 0; i < appInfo.size(); i++) {
            appTitles[i] = appInfo.get(i).loadLabel(getActivity().getPackageManager()).toString();
            appCared[i] = banned.contains(appInfo.get(i).packageName);
        }

        // Make the dialog
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(R.string.ui_chooseApp);
        dialog.setMultiChoiceItems(appTitles, appCared, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                ArrayList<String> banned = new ArrayList<>(Arrays.asList(prefs.getString(key, defaults).split(",")));
                if (isChecked) {
                    banned.add(appInfo.get(which).packageName);
                } else {
                    banned.remove(appInfo.get(which).packageName);
                }
                String newBanned = "";
                for (String bannedFragment : banned) {
                    if (!bannedFragment.trim().isEmpty()) newBanned += bannedFragment.trim() + ",";
                }
                prefs.edit().putString(key, newBanned).apply();
            }
        });
        dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
        return false;
    }
}
