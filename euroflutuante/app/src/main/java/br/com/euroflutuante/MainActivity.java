package br.com.euroflutuante;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.*;
public class MainActivity extends Activity {
 @Override public void onCreate(Bundle b) {
 super.onCreate(b);
 LinearLayout root = new LinearLayout(this);
 root.setOrientation(LinearLayout.VERTICAL); root.setPadding(40,70,40,30);
 TextView title=new TextView(this); title.setText("€ Euro Flutuante"); title.setTextSize(27); root.addView(title);
 TextView desc=new TextView(this); desc.setText("Cotação indicativa EUR/BRL. Atualização a cada 5 minutos enquanto ativo. Arraste a etiqueta para posicioná-la. Não é a taxa final da Revolut."); desc.setTextSize(16); desc.setPadding(0,25,0,30); root.addView(desc);
 Button start=new Button(this); start.setText("ATIVAR COTAÇÃO FLUTUANTE"); root.addView(start);
 Button stop=new Button(this); stop.setText("DESATIVAR"); root.addView(stop);
 TextView note=new TextView(this); note.setText("No Xiaomi/POCO, permita exibir sobre outros apps e execução em segundo plano. A etiqueta fica abaixo da barra de status."); note.setPadding(0,30,0,0); root.addView(note);
 root.setGravity(Gravity.TOP); setContentView(root);
 start.setOnClickListener(v->{
 if(!Settings.canDrawOverlays(this)){startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));return;}
 if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=getPackageManager().PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},10);
 try{startForegroundService(new Intent(this,EuroService.class));}catch(Exception e){note.setText("Não foi possível iniciar: "+e.getMessage());}
 });
 stop.setOnClickListener(v->stopService(new Intent(this,EuroService.class)));
 }
}