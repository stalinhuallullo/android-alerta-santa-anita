package gob.pe.munisantanita.alertasantaanita.presentation.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
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
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import gob.pe.munisantanita.alertasantaanita.BuildConfig;
import gob.pe.munisantanita.alertasantaanita.R;
import gob.pe.munisantanita.alertasantaanita.presentation.util.FileUtils;
import gob.pe.munisantanita.alertasantaanita.presentation.util.Tools;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class AlertaActivity  extends AppCompatActivity implements View.OnClickListener {

    private String url = "" + BuildConfig.base_url + BuildConfig._api;

    LinearLayout lCamara;
    AppCompatEditText txtMensaje;
    ImageView imgCarama;
    Button btnAlertar;
    String rutaImagen = "";
    ProgressDialog pd;
    View mView;
    Dialog dialog;

    Button dialogBtnAceptar;
    TextView dialogMensajeRespuesta;
    LocationManager locationManager;
    Criteria criteria;
    Context context = this;
    String categoria = "";
    String id_categoria = "";
    Uri imageUri;

    public static final int REQUEST_CODE_TAKE_PHOTO = 3;
    public static final int REQUEST_CODE_TAKE_LOCATION = 4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerta);


        lCamara = findViewById(R.id.lCamara);
        txtMensaje = findViewById(R.id.txtMensaje);
        imgCarama = findViewById(R.id.imgCarama);
        btnAlertar = findViewById(R.id.btnAlertar);

        lCamara.setOnClickListener(this);
        btnAlertar.setOnClickListener(this);


        categoria = getIntent().getStringExtra("categoria");
        id_categoria = getIntent().getStringExtra("id");

        initToolbar();
        locationEnabled();
        checkPermissions("location");
        //mostrarDialog("taylor", "final");
    }



    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(categoria);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Tools.setSystemBarColor(this, R.color.colorPrimary);
    }

    private void showProgress() {
        pd = new ProgressDialog(this);
        pd.setMessage("Cargando...");
        pd.setCancelable(false);
        pd.setProgressStyle(R.color.colorPrimary);
        pd.show();
    }

    public void hiddenProgress() {
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
            pd = null;
        }
    }

    private void mostrarDialog(final String mensaje, final String tipo) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
                if (tipo.equals("mensaje_error")) dialog.setContentView(R.layout.dialog_reportar_mensaje_error);
                else dialog.setContentView(R.layout.dialog_reportar_respuesta);
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
                            onBackPressed();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lCamara:
                if (checkPermissions("camera")) {
                    openCamara();
                }
                break;
            case R.id.btnAlertar:
                if (checkPermissions("location"))  enviarAlerta();
                break;
        }
    }

    private void openCamara(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, categoria);
        values.put(MediaStore.Images.Media.DESCRIPTION, "");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, REQUEST_CODE_TAKE_PHOTO);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                openCamara();
            }
        }

    }

    private boolean checkPermissions(String option) {
        hiddenProgress();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_TAKE_PHOTO && resultCode == RESULT_OK) {
            showProgress();

            imgCarama.setImageURI(imageUri);
            File photoFile = FileUtils.getFile(this, imageUri);

            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("directorio", "cemvi")
                    .addFormDataPart("file", photoFile.getName(), RequestBody.create(MediaType.parse("multipart/form-data;"), photoFile))
                    .build();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url +"alertas/upload")
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .addHeader("cache-control", "no-cache")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("onFailure==>",  e.getMessage());
                    hiddenProgress();
                    call.cancel();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException { //
                    String data = response.body().string();
                    try {
                        JSONObject obj = new JSONObject(data);
                        if(obj.get("status").toString() == "success" || obj.get("status").toString().equals("success")){
                            JSONArray jsonArray = obj.getJSONArray("data");
                            if(jsonArray.length() > 0){

                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        imgCarama.setImageURI(imageUri);
                                    }
                                });

                                Log.e("ssssss", jsonArray.getJSONObject(0).getString("directorio"));
                                Log.e("ssssss", jsonArray.getJSONObject(0).getString("archivo"));
                                rutaImagen = jsonArray.getJSONObject(0).getString("archivo");
                            }
                            else {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        //imgCarama.setImageURI(imageUri);
                                        imgCarama.setImageResource(R.drawable.img_captura_camara);
                                        Toast.makeText(getApplication(), "No se logro subir la foto", Toast.LENGTH_SHORT).show();
                                    }
                                });


                            }

                            hiddenProgress();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("ERROR_IMAGEN_CAMARA", e.getMessage());
                        hiddenProgress();
                    }

                }
            });

        }
    }



    private void enviarAlerta() {
        locationEnabled();
        String mensaje = txtMensaje.getText().toString().trim();
        Log.e("rutaImagen =>", rutaImagen+" ===================");

        if (rutaImagen.length() <= 0)  mostrarDialog("Tomar una foto del momento", "mensaje_error");
        else if (categoria.length() <= 0)  mostrarDialog("Seleccione una categoria", "mensaje_error");
        else if (mensaje.length() <= 0) mostrarDialog("Describe el detalle de la emergencia", "mensaje_error");
        else {

            if (checkPermissions("location")) {
                try {

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    } else {
                        showProgress();
                        Location location = getLastKnownLocation();

                        JsonObject json = new JsonObject();
                        json.addProperty("descripcion", mensaje);
                        json.addProperty("archivo", rutaImagen);
                        json.addProperty("longitud", location.getLongitude());
                        json.addProperty("latitud", location.getLatitude());
                        json.addProperty("categorias", categoria);
                        Log.e("json ====>", json + "");

                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url(url +"alertas")
                                .addHeader("Content-Type", "application/json; charset=utf-8")
                                .addHeader("cache-control", "no-cache")
                                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json+""))
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.e("onFailure==>",  e.getMessage());
                                hiddenProgress();
                                call.cancel();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException { //
                                String data = response.body().string();
                                try {
                                    JSONObject jsonObject = new JSONObject(data);
                                    hiddenProgress();
                                    if(jsonObject.length() > 0){
                                        //hiddenProgress();
                                        Log.e("status ====>", jsonObject.get("status").toString());
                                        if(jsonObject.get("code").equals("409") || jsonObject.get("status").equals("invalidPosition")) mostrarDialog(jsonObject.get("message").toString(), "mensaje_error");
                                        else if (jsonObject.get("code").equals("400") || jsonObject.get("status").equals("invalidInput")) mostrarDialog(jsonObject.get("message").toString(), "mensaje_error");
                                        else mostrarDialog("Gracias por apoyarnos", "final");
                                    }


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    hiddenProgress();
                                }

                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e("GPS Exception ====>", e.getMessage() + "");
                    hiddenProgress();
                }
            }
            //hiddenProgress();
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
            e.printStackTrace() ;
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager. NETWORK_PROVIDER ) ;
        } catch (Exception e) {
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
