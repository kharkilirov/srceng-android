package me.nillerusr;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Typeface;

import com.valvesoftware.source.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LauncherActivity extends Activity {
    public static String PKG_NAME;
    public static boolean can_write = true;
    static EditText cmdArgs, GamePath = null, EnvEdit;
    public SharedPreferences mPref;
    public static final int sdk = Integer.valueOf(Build.VERSION.SDK).intValue();
    final static int REQUEST_PERMISSIONS = 42;

    public void applyPermissions(final String permissions[], final int code) {
        List<String> requestPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                requestPermissions.add(permission);
        }

        if (!requestPermissions.isEmpty()) {
            String[] requestPermissionsArray = new String[requestPermissions.size()];
            for (int i = 0; i < requestPermissions.size(); i++)
                requestPermissionsArray[i] = requestPermissions.get(i);
            requestPermissions(requestPermissionsArray, code);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, R.string.srceng_launcher_error_no_permission, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    public static String getDefaultDir() {
        File dir = Environment.getExternalStorageDirectory();
        if (dir == null || !dir.exists())
            return "/sdcard/";
        return dir.getPath();
    }

   