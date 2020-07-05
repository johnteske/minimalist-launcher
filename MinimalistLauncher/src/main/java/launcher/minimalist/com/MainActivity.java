package launcher.minimalist.com;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    private PackageManager packageManager;
    private ArrayList<String> packageNames;
    private ArrayAdapter<String> adapter;
    private ListView listView;

    private Map<String, String> displayNames = new HashMap<>();
    private static final String HIDE = "HIDE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup UI elements
        listView = new ListView(this);
        listView.setVerticalScrollBarEnabled(false);
        listView.setId(android.R.id.list);
        listView.setDivider(null);
        setContentView(listView);
        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) listView.getLayoutParams();

        // Left align with clock and use same vertical margin
        int margin = 30;
        p.setMargins(margin, margin, 0, margin);

        // Get a list of all the apps installed
        packageManager = getPackageManager();
        adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        packageNames = new ArrayList<>();

        // Tap on an item in the list to launch the app
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    startActivity(packageManager.getLaunchIntentForPackage(packageNames.get(position)));
                } catch (Exception e) {
                    fetchAppList();
                }
            }
        });

        // Long press on an item in the list to open the app settings
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    // Attempt to launch the app with the package name
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + packageNames.get(position)));
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    fetchAppList();
                }
                return false;
            }
        });

        initDisplayNames();
        fetchAppList();
    }

    private void initDisplayNames() {
        displayNames.put("andOTP", "Two-factor Authentication");
        displayNames.put("Bitwarden", "Password Manager");
        displayNames.put("Etar", "Calendar");
        displayNames.put("Nextcloud", "Cloud");
        displayNames.put("OsmAnd~", "Maps");
        displayNames.put("ProtonMail", "Email");
        displayNames.put("Tutanota", "Transactional email");
        displayNames.put("Signal", "Messages");
        displayNames.put("Vanadium", "Browser");
        displayNames.put("VoIP.ms SMS", "Transactional messages");

        displayNames.put("Auditor", HIDE);
        displayNames.put("DAVx\u2075", HIDE);
        displayNames.put("PDF Viewer", HIDE);
        displayNames.put("Messaging", HIDE);
        displayNames.put("Minimalist Launcher+", HIDE);
        displayNames.put("Scrambled Exif", HIDE);
        displayNames.put("Settings", HIDE);
    }

    private Comparator<ResolveInfo> DisplayNameComp = new Comparator<ResolveInfo>() {
        public int compare(ResolveInfo o1, ResolveInfo o2) {
            String packageLabel1 = (String) o1.loadLabel(packageManager);
            String displayName1 = displayNames.get(packageLabel1);
            String appName1 = displayName1 != null ? displayName1 : packageLabel1;

            String packageLabel2 = (String) o2.loadLabel(packageManager);
            String displayName2 = displayNames.get(packageLabel2);
            String appName2 = displayName2 != null ? displayName2 : packageLabel2;

            return appName1.compareTo(appName2);
        }
    };

    private void fetchAppList() {
        // Start from a clean adapter when refreshing the list
        adapter.clear();
        packageNames.clear();

        // Query the package manager for all apps
        List<ResolveInfo> activities = packageManager.queryIntentActivities(
                new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0);

        Collections.sort(activities, DisplayNameComp);

        for (ResolveInfo resolver : activities) {

            // Apply the display names and exclude apps marked HIDE
            String packageLabel = (String) resolver.loadLabel(packageManager);
            String displayName = displayNames.get(packageLabel);
            String appName = displayName != null ? displayName : packageLabel;
            if (appName.equals(HIDE))
                continue;

            adapter.add(appName);
            packageNames.add(resolver.activityInfo.packageName);
        }
        listView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        // Prevent the back button from closing the activity.
        fetchAppList();
    }
}
