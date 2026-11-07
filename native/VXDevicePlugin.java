package com.akij.versionx;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.Settings;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.util.List;

@CapacitorPlugin(name = "VXDevice")
public class VXDevicePlugin extends Plugin {
  @PluginMethod
  public void launchApp(PluginCall call) {
    String q = call.getString("name", "").toLowerCase().trim();
    PackageManager pm = getContext().getPackageManager();
    Intent main = new Intent(Intent.ACTION_MAIN, null);
    main.addCategory(Intent.CATEGORY_LAUNCHER);
    List<ResolveInfo> list = pm.queryIntentActivities(main, 0);
    ResolveInfo best = null;
    for (ResolveInfo r : list) {
      String n = r.loadLabel(pm).toString().toLowerCase();
      if (n.equals(q)) { best = r; break; }
      if (best == null && !n.isEmpty() && !q.isEmpty() && (n.contains(q) || q.contains(n))) best = r;
    }
    JSObject o = new JSObject();
    if (best == null) { o.put("ok", false); call.resolve(o); return; }
    Intent i = pm.getLaunchIntentForPackage(best.activityInfo.packageName);
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    getContext().startActivity(i);
    o.put("ok", true);
    o.put("label", best.loadLabel(pm).toString());
    call.resolve(o);
  }

  @PluginMethod
  public void flashlight(PluginCall call) {
    try {
      CameraManager cm = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
      cm.setTorchMode(cm.getCameraIdList()[0], call.getBoolean("on", true));
      call.resolve();
    } catch (Exception e) { call.reject(e.getMessage()); }
  }

  @PluginMethod
  public void volume(PluginCall call) {
    AudioManager am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
    int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    am.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round(max * call.getInt("level", 50) / 100f), AudioManager.FLAG_SHOW_UI);
    call.resolve();
  }

  @PluginMethod
  public void settings(PluginCall call) {
    String p = call.getString("page", "");
    String a = Settings.ACTION_SETTINGS;
    switch (p) {
      case "wifi": a = Settings.ACTION_WIFI_SETTINGS; break;
      case "bluetooth": a = Settings.ACTION_BLUETOOTH_SETTINGS; break;
      case "display": a = Settings.ACTION_DISPLAY_SETTINGS; break;
      case "sound": a = Settings.ACTION_SOUND_SETTINGS; break;
      case "battery": a = Intent.ACTION_POWER_USAGE_SUMMARY; break;
      case "location": a = Settings.ACTION_LOCATION_SOURCE_SETTINGS; break;
      case "airplane": a = Settings.ACTION_AIRPLANE_MODE_SETTINGS; break;
      case "apps": a = Settings.ACTION_APPLICATION_SETTINGS; break;
    }
    Intent i = new Intent(a);
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    getContext().startActivity(i);
    call.resolve();
  }

  @PluginMethod
  public void dial(PluginCall call) {
    Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + call.getString("number", "")));
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    getContext().startActivity(i);
    call.resolve();
  }
}
