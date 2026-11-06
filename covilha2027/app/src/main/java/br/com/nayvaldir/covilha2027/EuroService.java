package br.com.nayvaldir.covilha2027;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.TextView;
import org.json.JSONObject;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
public class EuroService extends Service {
 private static final String CHANNEL="euro_cotacao";
 private WindowManager wm; private TextView label; private WindowManager.LayoutParams params;
 private final Handler main=new Handler(Looper.getMainLooper());
 private final ExecutorService io=Executors.newSingleThreadExecutor();
 private volatile boolean running=false;
 private final Runnable refresh=new Runnable(){
 @Override public void run(){
 if(!running)return;
 io.execute(()->{
 String result;
 try{
 HttpURLConnection conn=(HttpURLConnection)new URL("https://economia.awesomeapi.com.br/json/last/EUR-BRL").openConnection();
 conn.setConnectTimeout(10000);conn.setReadTimeout(10000);conn.setRequestProperty("Accept","application/json");
 try(InputStream stream=conn.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){
 byte[] buf=new byte[4096];int n;while((n=stream.read(buf))!=-1)out.write(buf,0,n);
 JSONObject quote=new JSONObject(out.toString("UTF-8")).getJSONObject("EURBRL");
 double mid=(quote.getDouble("bid")+quote.getDouble("ask"))/2.0;
 result="€ "+String.format(new Locale("pt","BR"),"%.3f",mid);
 getSharedPreferences("quote",MODE_PRIVATE).edit().putString("last",result).apply();
 }finally{conn.disconnect();}
 }catch(Exception e){result=getSharedPreferences("quote",MODE_PRIVATE).getString("last","€ --,---")+" · offline";}
 final String display=result;main.post(()->{if(label!=null)label.setText(display);});
 });
 main.postDelayed(this,5*60*1000L);
 }};
 @Override public void onCreate(){
 super.onCreate();
 NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
 nm.createNotificationChannel(new NotificationChannel(CHANNEL,"Cotação do euro",NotificationManager.IMPORTANCE_LOW));
 PendingIntent pi=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
 Notification notification=new Notification.Builder(this,CHANNEL).setSmallIcon(android.R.drawable.ic_menu_info_details).setContentTitle("Euro Flutuante ativo").setContentText("Toque para desativar").setContentIntent(pi).setOngoing(true).build();
 startForeground(1,notification);
 if(!Settings.canDrawOverlays(this)){stopSelf();return;}
 wm=(WindowManager)getSystemService(WINDOW_SERVICE);
 label=new TextView(this);label.setText("€ --,---");label.setTextSize(15);label.setTextColor(Color.WHITE);label.setPadding(20,10,20,10);
 GradientDrawable bg=new GradientDrawable();bg.setColor(0xDD18251D);bg.setCornerRadius(28);label.setBackground(bg);
 params=new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
 params.gravity=Gravity.TOP|Gravity.START;params.x=12;params.y=80;
 label.setOnTouchListener(new View.OnTouchListener(){
 float downX,downY;int startX,startY;
 @Override public boolean onTouch(View v,MotionEvent e){
 switch(e.getAction()){
 case MotionEvent.ACTION_DOWN:downX=e.getRawX();downY=e.getRawY();startX=params.x;startY=params.y;return true;
 case MotionEvent.ACTION_MOVE:params.x=Math.max(0,startX+(int)(e.getRawX()-downX));params.y=Math.max(0,startY+(int)(e.getRawY()-downY));if(wm!=null&&label!=null)wm.updateViewLayout(label,params);return true;
 case MotionEvent.ACTION_UP:return true;
 }return false;
 }});
 wm.addView(label,params);running=true;main.post(refresh);
 }
 @Override public int onStartCommand(Intent i,int flags,int startId){return START_STICKY;}
 @Override public void onDestroy(){running=false;main.removeCallbacks(refresh);io.shutdownNow();if(wm!=null&&label!=null){wm.removeView(label);label=null;}super.onDestroy();}
 @Override public IBinder onBind(Intent intent){return null;}
}