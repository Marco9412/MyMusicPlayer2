package com.panni.mymusicplayer2.view.settings;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.Toast;

import com.panni.mymusicplayer2.BuildConfig;
import com.panni.mymusicplayer2.R;
import com.panni.mymusicplayer2.controller.ControllerImpl;
import com.panni.mymusicplayer2.controller.localsongs.CustomSongManager;
import com.panni.mymusicplayer2.controller.localsongs.LocalSongManager;
import com.panni.mymusicplayer2.controller.queue.QueueManager;
import com.panni.mymusicplayer2.controller.upload.UploadSongActivity;
import com.panni.mymusicplayer2.controller.youtube.YoutubeLinkActivity;
import com.panni.mymusicplayer2.settings.Settings;
import com.panni.mymusicplayer2.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            preference.setSummary(stringValue);


           // if (preference.getKey().equals("yt_intent_enable")) {

            //}

            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        if (preference == null) return;

        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    private static void registerListener(final Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                //if (key.equals("remote_addr") || key.equals("remote_password") || key.equals("remote_username") || key.equals("remote_https_port"))
                //    ControllerImpl.getInstance().updatePreferences(context);

                if (key.equals("yt_intent_enable"))
                    YoutubeLinkActivity.setEnabled(context, sharedPreferences.getBoolean(key, false));

                if (key.equals("upload_enable"))
                    UploadSongActivity.setEnabled(context, sharedPreferences.getBoolean(key, false));

                ControllerImpl.getInstance().settingsChanged(Settings.loadSettings(context));
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        registerListener(this);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || SharePreferenceFragment.class.getName().equals(fragmentName)
                || PlayerPreferenceFragment.class.getName().equals(fragmentName)
                || (DebugPreferenceFragment.class.getName().equals(fragmentName) && BuildConfig.DEBUG)
                || AppInfoPreferenceFragment.class.getName().equals(fragmentName);
                //|| DataSyncPreferenceFragment.class.getName().equals(fragmentName)
                //|| NotificationPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @SuppressLint("SimpleDateFormat")
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            //bindPreferenceSummaryToValue(findPreference("example_text"));
            //bindPreferenceSummaryToValue(findPreference("example_list"));
            bindPreferenceSummaryToValue(findPreference("remote_username"));
            bindPreferenceSummaryToValue(findPreference("remote_password"));
            bindPreferenceSummaryToValue(findPreference("remote_addr"));
            bindPreferenceSummaryToValue(findPreference("remote_https_port"));
            bindPreferenceSummaryToValue(findPreference("remote_http_port"));
            //bindPreferenceSummaryToValue(findPreference("dropbox_get_remote_addr"));
            //bindPreferenceSummaryToValue(findPreference("dropbox_username"));
            //bindPreferenceSummaryToValue(findPreference("dropbox_password"));
            //bindPreferenceSummaryToValue(findPreference("dropbox_file"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SharePreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_share);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            //bindPreferenceSummaryToValue(findPreference("yt_intent_enable"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PlayerPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_player);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            //bindPreferenceSummaryToValue(findPreference("yt_intent_enable"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DebugPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_debug);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            //bindPreferenceSummaryToValue(findPreference("yt_intent_enable"));

            findPreference("export_settings").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    File db = new File("./databases/LocalSongs.db");
                    File local = new File(getActivity().getFilesDir(), LocalSongManager.LOCAL_SONGS_FILE);
                    File custom = new File(getActivity().getFilesDir(), CustomSongManager.CUSTOM_SONG_FILE);
                    File queue = new File(getActivity().getFilesDir(), QueueManager.QUEUE_FILE_NAME);

                    try {
                        Utils.copy(db, new File(Environment.getExternalStorageDirectory().getPath(), "LocalSongs.db"));
                        Utils.copy(local, new File(Environment.getExternalStorageDirectory().getPath(), LocalSongManager.LOCAL_SONGS_FILE));
                        Utils.copy(custom, new File(Environment.getExternalStorageDirectory().getPath(), CustomSongManager.CUSTOM_SONG_FILE));
                        Utils.copy(queue, new File(Environment.getExternalStorageDirectory().getPath(), QueueManager.QUEUE_FILE_NAME));

                        Toast.makeText(getActivity(), "Files copied in sdcard", Toast.LENGTH_LONG).show();
                        return true;
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    return false;
                }
            });
            findPreference("clear_local").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    File local = new File(getActivity().getFilesDir(), LocalSongManager.LOCAL_SONGS_FILE);
                    local.delete();

                    Toast.makeText(getActivity(), "LocalSongs file deleted", Toast.LENGTH_LONG).show();
                    return true;
                }
            });
            findPreference("clear_queue").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    File queue = new File(getActivity().getFilesDir(), QueueManager.QUEUE_FILE_NAME);
                    queue.delete();

                    Toast.makeText(getActivity(), "Queue file deleted", Toast.LENGTH_LONG).show();
                    return true;
                }
            });
            findPreference("clear_custom").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    File custom = new File(getActivity().getFilesDir(), CustomSongManager.CUSTOM_SONG_FILE);
                    custom.delete();

                    Toast.makeText(getActivity(), "Custom songs file deleted", Toast.LENGTH_LONG).show();
                    return true;
                }
            });
            findPreference("load_existing_media").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Utils.runInThread(new Runnable() {
                        @Override
                        public void run() {
                            LocalSongManager.findLocalSongs(getActivity());
                        }
                    });

                    Toast.makeText(getActivity(), "Searching for local songs...", Toast.LENGTH_LONG).show();
                    return true;
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AppInfoPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_appinfo);
            setHasOptionsMenu(true);

            findPreference("pref_build_date").setTitle("Build date: " +
                    new SimpleDateFormat("yyyy-mm-dd hh:mm:ss").format(BuildConfig.BUILD_DATE));
            findPreference("pref_git_branch").setTitle("Git branch: " + BuildConfig.GitBranchName);
            findPreference("pref_git_hash").setTitle("Git commit: " + BuildConfig.GitHash);
            findPreference("pref_app_is_debug").setTitle("App build type: " + BuildConfig.BUILD_TYPE);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    /*
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     *
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
    */
}
