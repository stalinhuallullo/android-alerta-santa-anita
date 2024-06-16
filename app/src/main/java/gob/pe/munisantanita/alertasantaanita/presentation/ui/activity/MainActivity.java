package gob.pe.munisantanita.alertasantaanita.presentation.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import gob.pe.munisantanita.alertasantaanita.BuildConfig;
import gob.pe.munisantanita.alertasantaanita.R;
import gob.pe.munisantanita.alertasantaanita.presentation.util.Tools;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity  extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, MenuItem.OnMenuItemClickListener {

    private String url = "" + BuildConfig.base_url + BuildConfig._api;
    ActionBar actionBar;
    Toolbar toolbar;
    Button btnAlerta1;
    Button btnAlerta2;
    Button btnAlerta3;
    Button btnAlerta4;
    Button btnAlerta5;
    Button btnAlerta6;
    GifImageView btnToqueDeQueda1;
    private int countClick = 0;
    CountDownTimer countDownTimer = null;
    LocationManager locationManager;
    Criteria criteria;
    View parent_view;
    Dialog dialog;
    Context context = this;
    TextView dialogMensajeRespuesta;
    Button dialogBtnAceptar;
    public static final int REQUEST_CODE_TAKE_PHOTO = 3;
    public static final int REQUEST_CODE_TAKE_LOCATION = 4;
    public static final int MULTIPLE_PERMISSIONS = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAlerta1 = findViewById(R.id.btnAlerta1);
        btnAlerta2 = findViewById(R.id.btnAlerta2);
        btnAlerta3 = findViewById(R.id.btnAlerta3);
        btnAlerta4 = findViewById(R.id.btnAlerta4);
        btnAlerta5 = findViewById(R.id.btnAlerta5);
        btnAlerta6 = findViewById(R.id.btnAlerta6);
        btnToqueDeQueda1 = findViewById(R.id.btnToqueDeQueda1);
        parent_view = findViewById(android.R.id.content);

        btnAlerta1.setOnClickListener(this);
        btnAlerta2.setOnClickListener(this);
        btnAlerta3.setOnClickListener(this);
        btnAlerta4.setOnClickListener(this);
        btnAlerta5.setOnClickListener(this);
        btnAlerta6.setOnClickListener(this);
        btnToqueDeQueda1.setOnClickListener(this);

        initToolbar();
        initNavigationMenu();
        locationEnabled();
        checkPermissions("location");
    }


    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        TextView txtTitle = toolbar.findViewById(R.id.toolbar_title);
        txtTitle.setText("Alerta Santa Anita");
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(null);

        Tools.setSystemBarColor(this, R.color.colorPrimary);
    }

    private boolean checkPermissions(String option) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            switch (option) {
                case "camera":
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                                new String[]{
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                },
                                REQUEST_CODE_TAKE_PHOTO);

                        return false;
                    }
                    break;
                case "location":

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                                new String[]{
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                },
                                REQUEST_CODE_TAKE_LOCATION);
                        return false;
                    }
                    break;
            }

            return true;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_covid_19, menu);

        final MenuItem menu_notif = menu.findItem(R.id.nav_informar_covid_19_2);
        menu_notif.setOnMenuItemClickListener(this);

        return true;
    }


    private void initNavigationMenu() {
        NavigationView nav_view = findViewById(R.id.nav_view);
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        nav_view.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_informar_covid_19_2:
                showActivity("nav_informar_covid_19");
                break;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()){

            case R.id.nav_circle_1:
                showActivity("btnAlerta1");
                break;
            case R.id.nav_circle_2:
                showActivity("btnAlerta2");
                break;
            case R.id.nav_circle_3:
                showActivity("btnAlerta3");
                break;
            case R.id.nav_circle_4:
                showActivity("btnAlerta4");
                break;
            case R.id.nav_circle_5:
                showActivity("btnAlerta5");
                break;
            case R.id.nav_circle_6:
                showActivity("btnAlerta6");
                break;
            case R.id.nav_informar_covid_19:
                showActivity("nav_informar_covid_19");
                break;
            case R.id.nav_informar_covid_19_2:
                showActivity("nav_informar_covid_19");
                break;
            case android.R.id.home:
                super.onBackPressed();
                break;
        }
        return false;//super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnAlerta1:
                showActivity("btnAlerta1");
                break;
            case R.id.btnAlerta2:
                showActivity("btnAlerta2");
                break;
            case R.id.btnAlerta3:
                showActivity("btnAlerta3");
                break;
            case R.id.btnAlerta4:
                showActivity("btnAlerta4");
                break;
            case R.id.btnAlerta5:
                showActivity("btnAlerta5");
                break;
            case R.id.btnAlerta6:
                showActivity("btnAlerta6");
                break;
            case R.id.btnToqueDeQueda1:
                showActivity("btnToqueDeQueda1");
                break;
        }
    }
    private void mostrarSnackbar(String mensaje){
        Snackbar snackbar = Snackbar.make(parent_view, mensaje, Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(getResources().getColor(R.color.pink_background));
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(getResources().getColor(R.color.colorPreguntaTest));
        snackbar.setAction("Aceptar", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        snackbar.show();
    }

    private void mostrarDialog(final String mensaje, final String tipo) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
                dialog.setContentView(R.layout.dialog_reportar_sos);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setCancelable(false);

                dialogMensajeRespuesta = dialog.findViewById(R.id.dialogMensajeRespuesta);
                dialogBtnAceptar = dialog.findViewById(R.id.dialogBtnAceptar);

                dialogMensajeRespuesta.setText(mensaje);

                dialogBtnAceptar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (tipo.equals("final")) {
                            dialog.dismiss();
                        }
                        else ocultarDialog();
                    }
                });
                dialog.show();
            }
        });
    }

    private void ocultarDialog() {
        dialog.hide();
    }

    private void showActivity(String btn){
        switch (btn){
            case "btnAlerta1":
                startActivity(new Intent(this, ToqueDeQuedaActivity.class).putExtra("categoria", getResources().getString(R.string.circle_1)).putExtra("id", "1"));
                break;
            case "btnAlerta2":
                startActivity(new Intent(this, ToqueDeQuedaActivity.class).putExtra("categoria", getResources().getString(R.string.circle_2)).putExtra("id", "2"));
                break;
            case "btnAlerta3":
                startActivity(new Intent(this, ToqueDeQuedaActivity.class).putExtra("categoria", getResources().getString(R.string.circle_3)).putExtra("id", "3"));
                break;
            case "btnAlerta4":
                startActivity(new Intent(this, ToqueDeQuedaActivity.class).putExtra("categoria", getResources().getString(R.string.circle_4)).putExtra("id", "4"));
                break;
            case "btnAlerta5":
                startActivity(new Intent(this, ToqueDeQuedaActivity.class).putExtra("categoria", getResources().getString(R.string.circle_5)).putExtra("id", "5"));
                break;
            case "btnAlerta6":
                startActivity(new Intent(this, ToqueDeQuedaActivity.class).putExtra("categoria", getResources().getString(R.string.circle_6)).putExtra("id", "6"));
                break;

            case "btnToqueDeQueda1":
                //startActivity(new Intent(this, ToqueDeQuedaActivity.class));
                countClick++;
                if(countDownTimer != null) {
                    countDownTimer.cancel();
                    countDownTimer.start();
                } else {
                    countDownTimer= new CountDownTimer(1000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            if(countClick >= 3) {
                                /*Log.e("Abrir", "==================>  Abrir vista " + countClick+"");

                                Location location = getLastKnownLocation();
                                JsonObject json = new JsonObject();
                                json.addProperty("longitud", location.getLongitude());
                                json.addProperty("latitud", location.getLatitude());

                                OkHttpClient client = new OkHttpClient();
                                Request request = new Request.Builder()
                                        .url(url +"alertas/sos")
                                        .addHeader("Content-Type", "application/json; charset=utf-8")
                                        .addHeader("cache-control", "no-cache")
                                        .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json+""))
                                        .build();

                                client.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e){
                                        Log.e("onFailure==>",  e.getMessage());
                                        mostrarSnackbar(e.getMessage());
                                        call.cancel();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException { //
                                        String data = response.body().string();
                                        Log.e("Respuesta ====>", data);

                                        try {
                                            JSONObject jsonObject = new JSONObject(data);
                                            if(jsonObject.length() > 0){
                                                Log.e("status ====>", jsonObject.get("status").toString());
                                                if(jsonObject.get("code").equals("409") || jsonObject.get("status").equals("invalidPosition")) mostrarSnackbar( jsonObject.get("message").toString() );
                                                else if (jsonObject.get("code").equals("400") || jsonObject.get("status").equals("invalidInput")) mostrarSnackbar( jsonObject.get("message").toString() );
                                                else mostrarDialog("Gracias por apoyarnos", "final");
                                            }

                                        } catch (JSONException e) {
                                            mostrarSnackbar(e.getMessage());
                                            e.printStackTrace();
                                        }

                                    }
                                });
                                countDownTimer.cancel();*/
                            }
                            else Log.e("click ==>", countClick+"");
                        }
                        public void onFinish() {
                            Log.e("onFinish ===", "===================> " + countClick);
                            if(countClick >= 1 && countClick < 3) {
                                startActivity(new Intent(context, ToqueDeQuedaActivity.class).putExtra("categoria", getResources().getString(R.string.circle_7)).putExtra("id", "7"));
                                //Log.e("GGG", " Alerta normal ========");
                            } else {
                                Log.e("Abrir", "==================>  Abrir vista " + countClick+"");

                                Location location = getLastKnownLocation();
                                JsonObject json = new JsonObject();
                                json.addProperty("longitud", location.getLongitude());
                                json.addProperty("latitud", location.getLatitude());

                                OkHttpClient client = new OkHttpClient();
                                Request request = new Request.Builder()
                                        .url(url +"alertas/sos")
                                        .addHeader("Content-Type", "application/json; charset=utf-8")
                                        .addHeader("cache-control", "no-cache")
                                        .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json+""))
                                        .build();

                                client.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e){
                                        Log.e("onFailure==>",  e.getMessage());
                                        mostrarSnackbar(e.getMessage());
                                        call.cancel();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException { //
                                        String data = response.body().string();
                                        Log.e("Respuesta ====>", data);

                                        try {
                                            JSONObject jsonObject = new JSONObject(data);
                                            if(jsonObject.length() > 0){
                                                Log.e("status ====>", jsonObject.get("status").toString());
                                                //if(jsonObject.get("code").equals("409") && jsonObject.get("status").equals("invalidPosition")) mostrarSnackbar( jsonObject.get("message").toString() );
                                                if(jsonObject.get("code").equals(409) && jsonObject.get("status").equals("invalidPosition")) {
                                                    Log.e("status ====>", "adadads");
                                                    if(jsonObject.get("message").equals("ateInvalidPosition")) {
                                                        mostrarSnackbar( "Llamando al centro de emergencias de Ate Vitarte" );
                                                        llamar("01 417 7575");
                                                    }
                                                    else if(jsonObject.get("message").equals("elAgustinoInvalidPosition")) {
                                                        mostrarSnackbar( "Llamando al centro de emergencias del Agustino" );
                                                        llamar("(01) 385 1438");
                                                    }
                                                    else mostrarSnackbar("Se encuentra fuera de rango.");
                                                    Log.e("status ====>", "gggggggggggg");
                                                }
                                                else if (jsonObject.get("code").equals(400) || jsonObject.get("status").equals("invalidInput")) mostrarSnackbar( "Error al enviar las coordenadas. Inténtalo nuevamente" );
                                                else mostrarDialog("Su emergencia fue enviada con exito.", "final");
                                            }

                                        } catch (JSONException e) {
                                            mostrarSnackbar(e.getMessage());
                                            e.printStackTrace();
                                        }

                                    }
                                });
                                countDownTimer.cancel();
                            }
                            /*if(countClick >= 3) Log.e("GGG", " Alerta de emergencia ========");
                            else if(countClick >= 1 && countClick < 3) Log.e("GGG", " Alerta normal ========");*/
                            countClick = 0;
                        }
                    }.start();
                }
                break;
            case "nav_informar_covid_19":
                startActivity(new Intent(this, InformacionCovid19Activity.class));

                break;

        }
    }

    private void llamar(String number){
        Intent callIntent4 = new Intent(Intent.ACTION_CALL);
        callIntent4.setData(Uri.parse("tel:"+ number));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission
                                .WRITE_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE},
                        MULTIPLE_PERMISSIONS);
                return;
            }
            else startActivity(callIntent4);
        }
    }

    private Location getLastKnownLocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        criteria = new Criteria();
        List<String> providers = locationManager.getProviders(criteria,true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        return bestLocation;
    }
    private void locationEnabled () {
        LocationManager lm = (LocationManager)
                getSystemService(Context. LOCATION_SERVICE ) ;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager. GPS_PROVIDER ) ;
        } catch (Exception e) {
            mostrarSnackbar(e.getMessage());
            e.printStackTrace() ;
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager. NETWORK_PROVIDER ) ;
        } catch (Exception e) {
            mostrarSnackbar(e.getMessage());
            e.printStackTrace() ;
        }
        if (!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(this)
                    .setMessage( "Activar GPS" )
                    .setPositiveButton( "Configuraciones" , new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick (DialogInterface paramDialogInterface , int paramInt) {
                                    startActivity( new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS )) ;
                                }
                            })
                    .setNegativeButton( "Cancelar" , null )
                    .show() ;
        }
    }


}
