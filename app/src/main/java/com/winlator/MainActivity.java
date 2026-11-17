package com.winlator;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.provider.DocumentsContract;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.winlator.container.Container;
import com.winlator.container.ContainerManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.google.android.material.navigation.NavigationView;
import com.winlator.contentdialog.AboutDialog;
import com.winlator.core.AppUtils;
import com.winlator.core.Callback;
import com.winlator.core.LocaleHelper;
import com.winlator.core.PreloaderDialog;
import com.winlator.core.HttpUtils;
import com.winlator.xenvironment.RootFSInstaller;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Aoe3HomeFragment.HostActions {
    public static final boolean DEBUG_MODE = false; // FIXME change to false
    public static final @IntRange(from = 1, to = 19) byte CONTAINER_PATTERN_COMPRESSION_LEVEL = 9;
    public static final byte PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    public static final byte OPEN_FILE_REQUEST_CODE = 2;
    public static final byte EDIT_INPUT_CONTROLS_REQUEST_CODE = 3;
    public static final byte OPEN_DIRECTORY_REQUEST_CODE = 4;
    private static final int PICK_GAME_FOLDER_REQUEST_CODE = 5;
    private DrawerLayout drawerLayout;
    public final PreloaderDialog preloaderDialog = new PreloaderDialog(this);
    private boolean editInputControls = false;
    private int selectedProfileId;
    private Callback<Uri> openFileCallback;
    private SharedPreferences preferences;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppUtils.setActivityTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        drawerLayout = findViewById(R.id.DrawerLayout);
        NavigationView navigationView = findViewById(R.id.NavigationView);
        navigationView.setNavigationItemSelectedListener(this);

        setSupportActionBar(findViewById(R.id.Toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Intent intent = getIntent();
        editInputControls = intent.getBooleanExtra("edit_input_controls", false);
        if (editInputControls) {
            selectedProfileId = intent.getIntExtra("selected_profile_id", 0);
            actionBar.setHomeAsUpIndicator(R.drawable.icon_action_bar_back);
            onNavigationItemSelected(navigationView.getMenu().findItem(R.id.menu_item_input_controls));
            navigationView.setCheckedItem(R.id.menu_item_input_controls);
        }
        else {
            int selectedMenuItemId = intent.getIntExtra("selected_menu_item_id", 0);
            actionBar.setHomeAsUpIndicator(R.drawable.icon_action_bar_menu);

            if (selectedMenuItemId > 0) {
                onNavigationItemSelected(navigationView.getMenu().findItem(selectedMenuItemId));
                navigationView.setCheckedItem(selectedMenuItemId);
            }
            else {
                showFragment(new ShortcutsFragment());
            }

            requestAppPermissions();
            // RootFS lives in app-private storage and must not depend on legacy shared-storage permission.
            RootFSInstaller.installIfNeeded(this);

            int containerId = intent.getIntExtra("container_id", 0);
            String startPath = intent.getStringExtra("start_path");
            if (containerId > 0 && startPath != null) {
                showFragment(new ContainerFileManagerFragment(containerId, startPath));
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setSystemLocale(newBase));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                RootFSInstaller.installIfNeeded(this);
            }
            else {
                Toast.makeText(this, "Storage permission was not granted. You can still use Android's folder picker to import game files.", Toast.LENGTH_LONG).show();
                RootFSInstaller.installIfNeeded(this);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainActivity.OPEN_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (openFileCallback != null) {
                openFileCallback.call(data.getData());
                openFileCallback = null;
            }
        }
        else if (requestCode == PICK_GAME_FOLDER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Uri treeUri = data.getData();
            if (treeUri != null) {
                final int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(treeUri, flags);
                preferences.edit().putString("aoe3_game_folder_uri", treeUri.toString()).apply();
                chooseImportContainer(treeUri);
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if ((newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ||
            newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) && currentFragment instanceof BaseFileManagerFragment) {
            ((BaseFileManagerFragment)currentFragment).onOrientationChanged();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentFragment != null && currentFragment.isVisible()) {
            if (currentFragment instanceof BaseFileManagerFragment) {
                BaseFileManagerFragment fileManagerFragment = (BaseFileManagerFragment)currentFragment;
                if (fileManagerFragment.onBackPressed()) return;
            }
            else if (currentFragment instanceof ContainersFragment) {
                finish();
            }
        }

        showFragment(new ShortcutsFragment());
    }


    @Override
    public void onChooseGameFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, PICK_GAME_FOLDER_REQUEST_CODE);
    }

    private void chooseImportContainer(Uri treeUri) {
        ContainerManager manager = new ContainerManager(this);
        ArrayList<Container> containers = manager.getContainers();
        if (containers.isEmpty()) {
            Toast.makeText(this, "Create a Winlator container first, then select the game folder again.", Toast.LENGTH_LONG).show();
            showFragment(new ContainersFragment());
            return;
        }
        String[] names = new String[containers.size()];
        for (int i = 0; i < containers.size(); i++) names[i] = containers.get(i).getName();
        new AlertDialog.Builder(this)
            .setTitle("Import game into container")
            .setItems(names, (dialog, which) -> importGame(treeUri, containers.get(which)))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void importGame(Uri treeUri, Container container) {
        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Importing Age of Empires III");
        progress.setMessage("Copying game files into " + container.getName() + "...");
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.show();
        new Thread(() -> {
            try {
                GameImportManager.Result result = GameImportManager.importGame(this, treeUri, container);
                runOnUiThread(() -> {
                    progress.dismiss();
                    selectGameExecutable(container, result);
                });
            }
            catch (Exception e) {
                runOnUiThread(() -> {
                    progress.dismiss();
                    Toast.makeText(this, "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void selectGameExecutable(Container container, GameImportManager.Result result) {
        if (result.executables.isEmpty()) {
            new AlertDialog.Builder(this)
                .setTitle("Game files imported")
                .setMessage("The folder was copied, but no .exe files were detected. Your imported files are being kept in the container. You can rescan the existing copy after this screen is closed.")
                .setPositiveButton("OK", (dialog, which) -> showFragment(new Aoe3HomeFragment()))
                .setCancelable(false)
                .show();
            return;
        }

        ArrayList<String> labels = new ArrayList<>();
        for (java.io.File executable : result.executables) {
            String relative = executable.getAbsolutePath().substring(result.installDirectory.getAbsolutePath().length());
            relative = relative.startsWith(java.io.File.separator) ? relative.substring(1) : relative;
            labels.add("[" + GameImportManager.getExecutableArchitecture(executable) + "] " + relative);
        }
        String[] choices = labels.toArray(new String[0]);
        // Do not combine AlertDialog.setMessage() with setItems(): on some Android
        // versions/providers this leaves the dialog showing only its action button.
        // The title carries the count and the list itself remains the complete,
        // scrollable selection surface. Never delete a multi-gigabyte import here.
        new AlertDialog.Builder(this)
            .setTitle("Choose the game executable (" + choices.length + " found)")
            .setItems(choices, (dialog, which) -> {
                java.io.File executable = result.executables.get(which);
                preferences.edit()
                    .putInt("aoe3_container_id", container.id)
                    .putString("aoe3_executable_path", executable.getAbsolutePath())
                    .putString("aoe3_install_path", result.installDirectory.getAbsolutePath())
                    .apply();
                Toast.makeText(this, "Game executable selected: " + executable.getName(), Toast.LENGTH_LONG).show();
                showFragment(new Aoe3HomeFragment());
            })
            .setCancelable(true)
            .setNegativeButton("Keep imported files", (dialog, which) -> showFragment(new Aoe3HomeFragment()))
            .show();
    }

    @Override
    public void onLaunchGame() {
        int containerId = preferences.getInt("aoe3_container_id", 0);
        String executable = preferences.getString("aoe3_executable_path", null);
        if (containerId <= 0 || executable == null || !new java.io.File(executable).isFile()) {
            Toast.makeText(this, "Import and validate the game folder before launching.", Toast.LENGTH_LONG).show();
            return;
        }
        Container container = new ContainerManager(this).getContainerById(containerId);
        if (container == null) {
            preferences.edit().remove("aoe3_container_id").remove("aoe3_executable_path").remove("aoe3_install_path").apply();
            Toast.makeText(this, "The selected Winlator container no longer exists. Import the game again.", Toast.LENGTH_LONG).show();
            showFragment(new Aoe3HomeFragment());
            return;
        }

        if (!preferences.getBoolean("aoe3_vcruntime_installed", false)) {
            new AlertDialog.Builder(this)
                .setTitle("Install game runtime")
                .setMessage("Age of Empires III: Definitive Edition requires the Microsoft Visual C++ 2015–2022 runtime. This container currently has only the older VC++ 2010 component enabled. Install Microsoft's official 64-bit runtime into this container before launching?")
                .setPositiveButton("Install runtime", (dialog, which) -> installAoe3Runtime(container))
                .setNeutralButton("Try launching anyway", (dialog, which) -> launchAoe3Executable(containerId, executable))
                .setNegativeButton("Cancel", null)
                .show();
            return;
        }

        launchAoe3Executable(containerId, executable);
    }

    private void installAoe3Runtime(Container container) {
        File runtimeDirectory = new File(container.getUserDir(), "Downloads/A0E3-Runtime");
        if (!runtimeDirectory.isDirectory() && !runtimeDirectory.mkdirs()) {
            Toast.makeText(this, "Unable to create the runtime installer folder in the container.", Toast.LENGTH_LONG).show();
            return;
        }
        File installer = new File(runtimeDirectory, "vc_redist.x64.exe");
        if (installer.isFile() && installer.length() > 1024 * 1024) {
            launchAoe3RuntimeInstaller(container, installer);
            return;
        }

        HttpUtils.download(this, "https://aka.ms/vs/17/release/vc_redist.x64.exe", installer, (success) -> {
            if (success && installer.isFile() && installer.length() > 1024 * 1024) {
                launchAoe3RuntimeInstaller(container, installer);
            }
            else Toast.makeText(this, "Microsoft's VC++ runtime download failed. Your imported game files were not changed.", Toast.LENGTH_LONG).show();
        });
    }

    private void launchAoe3RuntimeInstaller(Container container, File installer) {
        Intent intent = new Intent(this, XServerDisplayActivity.class);
        intent.putExtra("container_id", container.id);
        intent.putExtra("exec_path", installer.getAbsolutePath());
        intent.putExtra("exec_args", "/install /quiet /norestart");
        intent.putExtra("mark_aoe3_vcruntime_installed", true);
        startActivity(intent);
    }

    private void launchAoe3Executable(int containerId, String executable) {
        Intent intent = new Intent(this, XServerDisplayActivity.class);
        intent.putExtra("container_id", containerId);
        intent.putExtra("exec_path", executable);
        startActivity(intent);
    }

    @Override
    public void onOpenContainers() {
        showFragment(new ContainersFragment());
    }

    @Override
    public void onOpenGraphicsSettings() {
        showFragment(new SettingsFragment());
    }

    @Override
    public void onOpenInputSettings() {
        showFragment(new InputControlsFragment(selectedProfileId));
    }

    public void setOpenFileCallback(Callback<Uri> openFileCallback) {
        this.openFileCallback = openFileCallback;
    }

    private boolean requestAppPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) return false;

        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == R.id.menu_item_add ||
            itemId == R.id.menu_item_home ||
            itemId == R.id.menu_item_view_style ||
            itemId == R.id.menu_item_new_folder) {
            return super.onOptionsItemSelected(menuItem);
        }
        else {
            if (editInputControls) {
                setResult(RESULT_OK);
                finish();
            }
            else {
                if (currentFragment instanceof BaseFileManagerFragment) {
                    BaseFileManagerFragment fileManagerFragment = (BaseFileManagerFragment)currentFragment;
                    if (fileManagerFragment.onOptionsMenuClicked()) return true;
                }
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        switch (item.getItemId()) {
            case R.id.menu_item_aoe3:
                showFragment(new Aoe3HomeFragment());
                break;
            case R.id.menu_item_shortcuts:
                preferences.edit().putBoolean("show_shortcuts_first", true).apply();
                showFragment(new ShortcutsFragment());
                break;
            case R.id.menu_item_containers:
                preferences.edit().putBoolean("show_shortcuts_first", false).apply();
                showFragment(new ContainersFragment());
                break;
            case R.id.menu_item_input_controls:
                showFragment(new InputControlsFragment(selectedProfileId));
                break;
            case R.id.menu_item_settings:
                showFragment(new SettingsFragment());
                break;
            case R.id.menu_item_about:
                (new AboutDialog(this)).show();
                break;
        }
        return true;
    }

    public void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
            .replace(R.id.FLFragmentContainer, fragment)
            .commit();

        drawerLayout.closeDrawer(GravityCompat.START);
        currentFragment = fragment;
    }
}