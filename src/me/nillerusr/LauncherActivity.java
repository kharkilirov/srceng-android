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

    public static String getAndroidDataDir() {
        String path = getDefaultDir() + "/Android/data/" + PKG_NAME + "/files";
        File directory = new File(path);
        if (!directory.exists())
            directory.mkdirs();
        return path;
    }

    public static void changeButtonsStyle(ViewGroup parent) {
        if (sdk >= 21)
            return;

        for (int i = parent.getChildCount() - 1; i >= 0; i--) {
            try {
                final View child = parent.getChildAt(i);

                if (child == null)
                    continue;

                if (child instanceof ViewGroup) {
                    changeButtonsStyle((ViewGroup) child);
                } else if (child instanceof Button) {
                    final Button b = (Button) child;
                    final Drawable bg = b.getBackground();
                    if (bg != null) bg.setAlpha(96);
                    b.setTextColor(0xFFFFFFFF);
                    b.setTextSize(15f);
                    b.setTypeface(b.getTypeface(), Typeface.BOLD);
                } else if (child instanceof EditText) {
                    final EditText b = (EditText) child;
                    b.setBackgroundColor(0xFF272727);
                    b.setTextColor(0xFFFFFFFF);
                    b.setTextSize(15f);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PKG_NAME = getApplication().getPackageName();
        requestWindowFeature(1);

        if (sdk >= 21)
            super.setTheme(0x01030224);
        else
            super.setTheme(0x01030005);

        mPref = getSharedPreferences("mod", 0);
        setContentView(R.layout.activity_launcher);

        LinearLayout body = (LinearLayout) findViewById(R.id.body);

        cmdArgs = (EditText) findViewById(R.id.edit_cmdline);
        EnvEdit = (EditText) findViewById(R.id.edit_env);
        GamePath = (EditText) findViewById(R.id.edit_gamepath);

        Button button = (Button) findViewById(R.id.button_launch);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LauncherActivity.this.startSource(v);
            }
        });

        Button aboutButton = (Button) findViewById(R.id.button_about);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Dialog dialog = new Dialog(LauncherActivity.this);
                dialog.setTitle(R.string.srceng_launcher_about);
                ScrollView scroll = new ScrollView(LauncherActivity.this);
                scroll.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                scroll.setPadding(5, 5, 5, 5);
                TextView text = new TextView(LauncherActivity.this);
                text.setText(R.string.srceng_launcher_about_text);
                text.setLinksClickable(true);
                text.setTextIsSelectable(true);
                Linkify.addLinks(text, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);
                scroll.addView(text);
                dialog.setContentView(scroll);
                dialog.show();
            }
        });

        Button dirButton = findViewById(R.id.button_gamedir);
        dirButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(LauncherActivity.this, DirchActivity.class);
                intent.addFlags(268435456);
                startActivity(intent);
            }
        });

        String last_commit = getResources().getString(R.string.last_commit);

        cmdArgs.setText(mPref.getString("argv", "-console"));
        GamePath.setText(mPref.getString("gamepath", getDefaultDir() + "/srceng/csgo"));
        EnvEdit.setText(mPref.getString("env", "LIBGL_USEVBO=0"));

        changeButtonsStyle((ViewGroup) this.getWindow().getDecorView());

        if (sdk >= 23)
            applyPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSIONS);
    }

    public void saveSettings(SharedPreferences.Editor editor) {
        String argv = cmdArgs.getText().toString();
        String gamepath = GamePath.getText().toString();
        String env = EnvEdit.getText().toString();

        editor.putString("argv", argv);
        editor.putString("gamepath", gamepath);
        editor.putString("env", env);
        editor.commit();
    }

    public void startSource(View view) {
        String gamepath = GamePath.getText().toString();

        SharedPreferences.Editor editor = mPref.edit();
        saveSettings(editor);

        if (sdk >= 19)
            editor.putBoolean("immersive_mode", true);
        else
            editor.putBoolean("immersive_mode", false);

        editor.commit();

        Intent intent = new Intent(LauncherActivity.this, SDLActivity.class);
        intent.addFlags(268435456);
        startActivity(intent);
    }

    public void onPause() {
        Log.v("SRCAPK", "onPause");
        saveSettings(mPref.edit());
        super.onPause();
    }
}
```
