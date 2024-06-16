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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

public class ToqueDeQuedaActivity  extends AppCompatActivity implements View.OnClickListener {

    private String url = "" + BuildConfig.base_url + BuildConfig._api;
    LinearLayout lCamara;
    AppCompatEditText txtCategoria;
    AppCompatEditText txtMensaje;
    ImageView imgCarama;
    Button btnAlertar;
    String rutaImagen = "";
    ProgressDialog pd;
    Dialog dialog;
    View parent_view;

    Button dialogBtnAceptar;
    TextView dialogMensajeRespuesta;
    LocationManager locationManager;
    Criteria criteria;
    Context context = this;
    Uri imageUri;
    String categoria = "";
    String id_categoria = "";
    String id_subcategoria = "";

    public static final int REQUEST_CODE_TAKE_PHOTO = 3;
    public static final int REQUEST_CODE_TAKE_LOCATION = 4;

    List<String> categorias = new ArrayList<>();
    //List<Categoria> categorias = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toque_de_queda);


        lCamara = findViewById(R.id.lCamara);
        txtCategoria = findViewById(R.id.txtCategoria);
        txtMensaje = findViewById(R.id.txtMensaje);
        imgCarama = findViewById(R.id.imgCarama);
        btnAlertar = findViewById(R.id.btnAlertar);

        lCamara.setOnClickListener(this);
        btnAlertar.setOnClickListener(this);

        categoria = getIntent().getStringExtra("categoria");
        id_categoria = getIntent().getStringExtra("id");
        parent_view = findViewById(android.R.id.content);

        //obtenerCategorias();
        initToolbar();
        locationEnabled();
        initComponent();
        checkPermissions("location");
        categorias.add("1 - Atropellos");
        categorias.add("2 - Choque de vehiculos");
        categorias.add("3 - Otros");
    }


    private void obtenerCategorias(){



        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url +"categorias/"+id_categoria)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("cache-control", "no-cache")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure==>",  e.getMessage());
                mostrarSnackbar(e.getMessage());
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException { //
                String data = response.body().string();
                try {
                    Log.e("data====>",  data);
                    JSONObject jsonObject = new JSONObject(data);

                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    int dataCategorias = jsonArray.length();
                    if(dataCategorias > 0){
                        for (int i = 0; i < dataCategorias; i++) {
                            JSONObject obj = (JSONObject) jsonArray.get(i);
                            categorias.add(obj.getInt("id") + "- " + obj.getString("nombre"));
                            //categorias.add(new Categoria(obj.getInt("id"), obj.getString("nombre")));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("Error", e.getMessage());
                    mostrarSnackbar(e.getMessage());
                }

            }
        });



    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(categoria);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Tools.setSystemBarColor(this, R.color.colorPrimary);

    }

    private void initComponent() {
        (findViewById(R.id.txtCategoria)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStateDialog(v);
            }
        });

    }

    private void showStateDialog(final View v) {

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, categorias);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(categoria);
        builder.setSingleChoiceItems(arrayAdapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                    ((EditText) v).setText(categorias.get(i).toString());
                dialogInterface.dismiss();
            }
        });
        builder.show();
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
        values.put(MediaStore.Images.Media.TITLE, "hola");
        values.put(MediaStore.Images.Media.DESCRIPTION, "descripcion");
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

        Log.e("RUTAAAAAAAAA", url +"alertas/upload");
        if (requestCode == REQUEST_CODE_TAKE_PHOTO && resultCode == RESULT_OK) {
            showProgress();

            //imgCarama.setImageURI(imageUri);
            File photoFile = FileUtils.getFile(this, imageUri);

            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("directorio", "cemvi")
                    .addFormDataPart("file", photoFile.getName(), RequestBody.create(MediaType.parse("multipart/form-data;"), photoFile))
                    .build();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    //.url("http://190.12.74.211/alertas-service/alertas/upload")
                    .url(url +"alertas/upload")
                    //.url("https://www.munisantanita.gob.pe/ApiAlertas/alertas/upload")
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .addHeader("cache-control", "no-cache")
                    .post(requestBody)
                    .build();



            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("onFailure==>",  e.getMessage());
                    mostrarSnackbar(e.getMessage());
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
                                        //Toast.makeText(getApplication(), "No se logro subir la foto", Toast.LENGTH_SHORT).show();
                                        Snackbar.make(parent_view, "No se logro subir la foto", Snackbar.LENGTH_SHORT).show();
                                    }
                                });


                            }

                            hiddenProgress();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("ERROR_IMAGEN_CAMARA", e.getMessage());
                        mostrarSnackbar(e.getMessage());
                        hiddenProgress();
                    }

                }
            });

        }
    }


    private void enviarAlerta() {
        locationEnabled();
        String mensaje = txtMensaje.getText().toString().trim();
        String sub_categoria = txtCategoria.getText().toString().trim();
        Log.e("rutaImagen =>", rutaImagen+" ===================");


        if (rutaImagen.length() <= 0)  mostrarSnackbar("Tomar una foto del momento");
        else if (categoria.length() <= 0)  mostrarSnackbar("Seleccione una categoria");
        else if (sub_categoria.length() <= 0)  mostrarSnackbar("Seleccione una categoria");
        else if (mensaje.length() <= 0) mostrarSnackbar("Describe el detalle de la emergencia");
        else {

            if (checkPermissions("location")) {
                try {
                    showProgress();
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    } else {
                        Location location = getLastKnownLocation();
                        JsonObject obj_sub = new JsonObject();
                        //new JsonObject().addProperty("id", sub_categoria.split("-")[0]);

                        /*String[] parts = categoria.split("-");
                        mostrarSnackbar(parts[0]);*/

                        JSONObject idCategoria = new JSONObject();
                        idCategoria.put("id", id_categoria);

                        JSONObject idSubCategoria = new JSONObject();
                        idSubCategoria.put("id", sub_categoria.split("-")[0]);

                        JSONObject json = new JSONObject();
                        json.put("descripcion", mensaje);
                        json.put("archivo", rutaImagen);
                        json.put("categoria", idCategoria);
                        json.put("subCategoria", idSubCategoria);
                        json.put("longitud", location.getLongitude());
                        json.put("latitud", location.getLatitude());
                        Log.e("json ====>", json + "");

                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url(url +"alertas/normal")
                                .addHeader("Content-Type", "application/json; charset=utf-8")
                                .addHeader("cache-control", "no-cache")
                                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json+""))
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e){
                                Log.e("onFailure==>",  e.getMessage());
                                mostrarSnackbar(e.getMessage());
                                hiddenProgress();
                                call.cancel();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException { //
                                String data = response.body().string();
                                Log.e("Respuesta ====>", data);

                                try {
                                    JSONObject jsonObject = new JSONObject(data);
                                    hiddenProgress();
                                    if(jsonObject.length() > 0){
                                        //hiddenProgress();
                                        Log.e("status ====>", jsonObject.get("status").toString());
                                        if(jsonObject.get("code").equals(409) || jsonObject.get("status").equals("invalidPosition")) mostrarSnackbar( jsonObject.get("message").toString() );
                                        else if (jsonObject.get("code").equals(400) || jsonObject.get("status").equals("invalidInput")) mostrarSnackbar( jsonObject.get("message").toString() );
                                        else mostrarDialog("Gracias por apoyarnos", "final");
                                    }

                                } catch (JSONException e) {
                                    mostrarSnackbar(e.getMessage());
                                    e.printStackTrace();
                                    hiddenProgress();
                                }

                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e("GPS Exception ====>", e.getMessage() + "");
                    mostrarSnackbar(e.getMessage());
                    hiddenProgress();
                }
            }
            hiddenProgress();
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

class Categoria {
    private int id;
    private String Nombre;

    public Categoria(int id, String nombre) {
        this.id = id;
        Nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
    }
}