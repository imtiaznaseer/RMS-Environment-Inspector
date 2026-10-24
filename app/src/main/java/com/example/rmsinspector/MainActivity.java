package com.example.rmsinspector;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionInfo;
import android.widget.*;
import java.io.File;
import java.util.*;

public class MainActivity extends Activity {
 TextView output;
 boolean showSensitive=false;
 String raw="";
 @Override public void onCreate(Bundle b) {
  super.onCreate(b);
  LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(20,20,20,20);
  TextView title=new TextView(this);title.setText("RMS Environment Inspector v1.0");title.setTextSize(21);root.addView(title);
  Button scan=new Button(this);scan.setText("Run local scan");root.addView(scan);
  Button perm=new Button(this);perm.setText("Allow phone identifier tests");root.addView(perm);
  Button toggle=new Button(this);toggle.setText("Show/hide sensitive values");root.addView(toggle);
  Button copy=new Button(this);copy.setText("Copy REDACTED report");root.addView(copy);
  ScrollView scroll=new ScrollView(this);output=new TextView(this);output.setTextIsSelectable(true);output.setTextSize(13);output.setPadding(4,12,4,12);scroll.addView(output);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
  scan.setOnClickListener(v->runScan());
  perm.setOnClickListener(v->{if(Build.VERSION.SDK_INT>=23)requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},101);});
  toggle.setOnClickListener(v->{showSensitive=!showSensitive;runScan();});
  copy.setOnClickListener(v->{((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("RMS redacted diagnostic",redact(raw)));Toast.makeText(this,"Redacted report copied",Toast.LENGTH_SHORT).show();});
  runScan();
 }
 static boolean emulator(){
  String manufacturer=Build.MANUFACTURER==null?"":Build.MANUFACTURER;
  String model=Build.MODEL==null?"":Build.MODEL;
  return Build.FINGERPRINT.startsWith("generic") || Build.FINGERPRINT.startsWith("unknown") || model.contains("google_sdk") || model.contains("Emulator") || model.contains("Android SDK built for x86") || Build.BOARD.contains("QC_Reference_Phone") || manufacturer.contains("Genymotion") || Build.HOST.startsWith("Build") || Build.BRAND.startsWith("generic") || Build.HARDWARE.contains("goldfish") || Build.HARDWARE.contains("ranchu") || Build.PRODUCT.contains("sdk_google") || Build.PRODUCT.contains("vbox86p");
 }
 static boolean rooted(){boolean e=emulator();String t=Build.TAGS;if((e||t==null||!t.contains("test-keys"))&&!new File("/system/app/Superuser.apk").exists())return !e&&new File("/system/xbin/su").exists();return true;}
 static String uuid(){String s="35"+(Build.BOARD.length()%10)+(Build.BRAND.length()%10)+(Build.CPU_ABI.length()%10)+(Build.DEVICE.length()%10)+(Build.DISPLAY.length()%10)+(Build.HOST.length()%10)+(Build.ID.length()%10)+(Build.MANUFACTURER.length()%10)+(Build.MODEL.length()%10)+(Build.PRODUCT.length()%10)+(Build.TAGS.length()%10)+(Build.TYPE.length()%10)+(Build.USER.length()%10);try{String serial;if(Build.VERSION.SDK_INT>=26&&androidxFreePermission())serial=Build.getSerial();else serial=Build.SERIAL;return new UUID(s.hashCode(),serial.hashCode()).toString();}catch(Exception ex){return new UUID(s.hashCode(),"serial".hashCode()).toString();}}
 static Context ctx;
 static boolean androidxFreePermission(){return ctx.checkSelfPermission(Manifest.permission.READ_PHONE_STATE)==PackageManager.PERMISSION_GRANTED;}
 static String reflectSlot(TelephonyManager tm,String method,int slot){try{Object o=tm.getClass().getMethod(method,int.class).invoke(tm,slot);return o==null?"":o.toString();}catch(Exception e){return "";}}
 static String imei(){TelephonyManager tm=(TelephonyManager)ctx.getSystemService(TELEPHONY_SERVICE);String[] methods={"getImei","getDeviceId","getDeviceIdDs","getDeviceIdGemini","getSimSerialNumberGemini"};for(int slot=0;slot<2;slot++)for(String m:methods){String v=reflectSlot(tm,m,slot);if(!v.isEmpty())return v;}return "000000000000000";}
 static String imsi(){if(androidxFreePermission()){try{SubscriptionManager sm=(SubscriptionManager)ctx.getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE);List<SubscriptionInfo> list=sm.getActiveSubscriptionInfoList();if(list!=null&&!list.isEmpty()){String v=list.get(0).getIccId();if(v!=null&&!v.isEmpty())return v;}}catch(Exception ignored){}return "000000000000000";}TelephonyManager tm=(TelephonyManager)ctx.getSystemService(TELEPHONY_SERVICE);String[] methods={"getSubscriberId","getSubscriberIdGemini","getSimSerialNumber"};for(int slot=0;slot<2;slot++)for(String m:methods){String v=reflectSlot(tm,m,slot);if(!v.isEmpty())return v;}return "000000000000000";}
 static String mask(String s){if(s==null)return "null";if(s.length()<7)return "[hidden]";return s.substring(0,2)+"…"+s.substring(s.length()-3);}
 static String redact(String s){return s.replaceAll("(?m)^(ANDROID_ID|RMS_UUID|IMEI_RESULT|IMSI_RESULT|BUILD_SERIAL):.*$","$1: [REDACTED]");}
 void line(StringBuilder sb,String k,Object v){sb.append(k).append(": ").append(v).append('\n');}
 void flag(StringBuilder sb,String name,boolean hit){line(sb,name,hit?"MATCH":"no match");}
 void runScan(){ctx=this;StringBuilder sb=new StringBuilder();line(sb,"RMS is_emulator",emulator());line(sb,"Crashlytics isRooted",rooted());sb.append("\nINDIVIDUAL RMS EMULATOR CHECKS\n");
 flag(sb,"fingerprint generic",Build.FINGERPRINT.startsWith("generic"));flag(sb,"fingerprint unknown",Build.FINGERPRINT.startsWith("unknown"));flag(sb,"model google_sdk",Build.MODEL.contains("google_sdk"));flag(sb,"model Emulator",Build.MODEL.contains("Emulator"));flag(sb,"model Android SDK built for x86",Build.MODEL.contains("Android SDK built for x86"));flag(sb,"board QC_Reference_Phone",Build.BOARD.contains("QC_Reference_Phone"));flag(sb,"manufacturer Genymotion",Build.MANUFACTURER.contains("Genymotion"));flag(sb,"host Build",Build.HOST.startsWith("Build"));flag(sb,"brand generic",Build.BRAND.startsWith("generic"));flag(sb,"hardware goldfish",Build.HARDWARE.contains("goldfish"));flag(sb,"hardware ranchu",Build.HARDWARE.contains("ranchu"));flag(sb,"product sdk_google",Build.PRODUCT.contains("sdk_google"));flag(sb,"product vbox86p",Build.PRODUCT.contains("vbox86p"));
 sb.append("\nBUILD FIELDS\n");line(sb,"FINGERPRINT",Build.FINGERPRINT);line(sb,"MODEL",Build.MODEL);line(sb,"MANUFACTURER",Build.MANUFACTURER);line(sb,"BOARD",Build.BOARD);line(sb,"HOST",Build.HOST);line(sb,"BRAND",Build.BRAND);line(sb,"HARDWARE",Build.HARDWARE);line(sb,"PRODUCT",Build.PRODUCT);line(sb,"TAGS",Build.TAGS);line(sb,"DEVICE",Build.DEVICE);line(sb,"DISPLAY",Build.DISPLAY);line(sb,"ID",Build.ID);line(sb,"TYPE",Build.TYPE);line(sb,"USER",Build.USER);line(sb,"OS_VERSION",Build.VERSION.RELEASE);
 sb.append("\nIDENTIFIERS (sensitive)\n");String androidId=Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);line(sb,"ANDROID_ID",androidId);line(sb,"RMS_UUID",uuid());line(sb,"IMEI_RESULT",imei());line(sb,"IMSI_RESULT",imsi());try{line(sb,"BUILD_SERIAL",Build.VERSION.SDK_INT>=26?Build.getSerial():Build.SERIAL);}catch(Exception e){line(sb,"BUILD_SERIAL","BLOCKED: "+e.getClass().getSimpleName());}
 line(sb,"PHONE_PERMISSION",androidxFreePermission());sb.append("\nNOTE: IMEI/IMSI results may be fallback placeholders. Android ID is app-signature scoped. No network permission.\n");raw=sb.toString();String visible=showSensitive?raw:redact(raw);output.setText(visible);}
 @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){super.onRequestPermissionsResult(requestCode,permissions,grantResults);runScan();}
}
