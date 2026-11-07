package br.com.nayvaldir.covilha2027;
import android.app.*;
import android.Manifest;
import android.provider.Settings;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.webkit.*;
import android.view.*;
import android.widget.*;
public class MainActivity extends Activity {
 private static final String HOME="https://nay-valdir-covilha-2027.nayaragomescn.chatgpt.site/";
 private WebView web; private ProgressBar progress;
 @Override public void onCreate(Bundle state){
 super.onCreate(state);
 getWindow().setStatusBarColor(Color.rgb(18,18,18));
 getWindow().setNavigationBarColor(Color.rgb(18,18,18));
 LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(0xff121212);
 LinearLayout controls=new LinearLayout(this);controls.setOrientation(LinearLayout.HORIZONTAL);controls.setPadding(4,0,4,0);
Button toggle=new Button(this);toggle.setText("€ Ativar");controls.addView(toggle,new LinearLayout.LayoutParams(0,48,1));
Button off=new Button(this);off.setText("Ocultar €");controls.addView(off,new LinearLayout.LayoutParams(0,48,1));
root.addView(controls,new LinearLayout.LayoutParams(-1,52));
toggle.setOnClickListener(v->{
 if(!Settings.canDrawOverlays(this)){
  startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));
  Toast.makeText(this,"Autorize a sobreposição e toque em Ativar novamente.",Toast.LENGTH_LONG).show();return;
 }
 if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=getPackageManager().PERMISSION_GRANTED)
  requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},12);
 try{startForegroundService(new Intent(this,EuroService.class));Toast.makeText(this,"Cotação flutuante ativada",Toast.LENGTH_SHORT).show();}
 catch(Exception e){Toast.makeText(this,"Falha ao iniciar: "+e.getMessage(),Toast.LENGTH_LONG).show();}
});
off.setOnClickListener(v->{stopService(new Intent(this,EuroService.class));Toast.makeText(this,"Cotação flutuante desativada",Toast.LENGTH_SHORT).show();});
progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(100);root.addView(progress,new LinearLayout.LayoutParams(-1,5));
 web=new WebView(this);web.setBackgroundColor(0xff121212);root.addView(web,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
 WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setDatabaseEnabled(true);s.setAllowFileAccess(false);s.setAllowContentAccess(true);s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);s.setMediaPlaybackRequiresUserGesture(false);
 CookieManager.getInstance().setAcceptCookie(true);CookieManager.getInstance().setAcceptThirdPartyCookies(web,true);
 web.setWebChromeClient(new WebChromeClient(){@Override public void onProgressChanged(WebView v,int p){progress.setProgress(p);progress.setVisibility(p==100?View.GONE:View.VISIBLE);}});
 web.setWebViewClient(new WebViewClient(){
 @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){
 Uri uri=r.getUrl();String scheme=uri.getScheme();if(scheme==null)return true;
 if((scheme.equals("https")||scheme.equals("http")) && uri.getHost()!=null && (uri.getHost().equals("nay-valdir-covilha-2027.nayaragomescn.chatgpt.site")||uri.getHost().endsWith(".chatgpt.site")))return false;
 try{startActivity(new Intent(Intent.ACTION_VIEW,uri));}catch(Exception ignored){}return true;
 }
 @Override public void onReceivedError(WebView v,WebResourceRequest req,WebResourceError err){
 if(req.isForMainFrame())Toast.makeText(MainActivity.this,"Sem conexão. Verifique a internet e tente novamente.",Toast.LENGTH_LONG).show();
 }
 });
 web.setDownloadListener((url,userAgent,contentDisposition,mimeType,length)->{
 try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(Exception e){Toast.makeText(this,"Não foi possível abrir o download",Toast.LENGTH_SHORT).show();}
 });
 if(state!=null)web.restoreState(state);else web.loadUrl(HOME);
 }
 @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
 @Override protected void onSaveInstanceState(Bundle out){web.saveState(out);super.onSaveInstanceState(out);}
 @Override protected void onDestroy(){if(web!=null){web.destroy();web=null;}super.onDestroy();}
}