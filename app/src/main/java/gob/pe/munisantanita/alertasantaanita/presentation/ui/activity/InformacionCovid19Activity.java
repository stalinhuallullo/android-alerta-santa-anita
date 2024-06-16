    package gob.pe.munisantanita.alertasantaanita.presentation.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import gob.pe.munisantanita.alertasantaanita.R;
import gob.pe.munisantanita.alertasantaanita.presentation.util.Tools;

    public class InformacionCovid19Activity extends AppCompatActivity implements View.OnClickListener {

    public static final int MULTIPLE_PERMISSIONS = 100;
    LinearLayout btnWhatsapp, btnWhatsapp2, btnPhoneSerenazgo1, btnPhoneSerenazgo2, btnPhoneCentral, btnPhoneEmergencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informacion_covid19);

        btnWhatsapp = findViewById(R.id.btnWhatsapp);
        btnWhatsapp2 = findViewById(R.id.btnWhatsapp2);

        btnPhoneCentral = findViewById(R.id.btnPhoneCentral);
        btnPhoneSerenazgo1 = findViewById(R.id.btnPhoneSerenazgo1);
        btnPhoneSerenazgo2 = findViewById(R.id.btnPhoneSerenazgo2);

        btnPhoneEmergencia = findViewById(R.id.btnPhoneEmergencia);

        btnWhatsapp.setOnClickListener(this);
        btnWhatsapp2.setOnClickListener(this);
        btnPhoneCentral.setOnClickListener(this);
        btnPhoneEmergencia.setOnClickListener(this);

        initToolbar();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comunícate con nosotros");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Tools.setSystemBarColor(this, R.color.colorPrimary);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnWhatsapp:
                try {
                    String mobile = "+51 939 249 716";
                    String msg = "¡Alo! Alerta Santa Anita \uD83D\uDEA8";
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=" + mobile + "&text=" + msg)));
                }
                catch (Exception e) { Toast.makeText(this, "Whatsapp no esta instalado", Toast.LENGTH_SHORT).show(); }
                break;
            case R.id.btnWhatsapp2:
                try {
                    String mobile = "+51 939 357 854";
                    String msg = "¡Alo! Alerta Santa Anita \uD83D\uDEA8";
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=" + mobile + "&text=" + msg)));
                }
                catch (Exception e) { Toast.makeText(this, "Whatsapp no esta instalado", Toast.LENGTH_SHORT).show(); }
                break;

            case R.id.btnPhoneCentral:
                llamar("(01) 207 8280");
                /*Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:(01) 207 8280"));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                                new String[]{Manifest.permission
                                        .WRITE_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE},
                                MULTIPLE_PERMISSIONS);
                        return;
                    }
                    else startActivity(callIntent);
                }*/
                break;
            case R.id.btnPhoneSerenazgo1:
                llamar("(01) 363 0396");
                /*Intent callIntent2 = new Intent(Intent.ACTION_CALL);
                callIntent2.setData(Uri.parse("tel:(01) 363-0396"));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                                new String[]{Manifest.permission
                                        .WRITE_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE},
                                MULTIPLE_PERMISSIONS);
                        return;
                    }
                    else startActivity(callIntent2);
                }*/
                break;
            case R.id.btnPhoneSerenazgo2:
                llamar("(01) 363 0397");
                /*Intent callIntent3 = new Intent(Intent.ACTION_CALL);
                callIntent3.setData(Uri.parse("tel:(01) 363-0397"));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                                new String[]{Manifest.permission
                                        .WRITE_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE},
                                MULTIPLE_PERMISSIONS);
                        return;
                    }
                    else startActivity(callIntent3);
                }*/
                break;
            case R.id.btnPhoneEmergencia:
                llamar("105");
                /*Intent callIntent4 = new Intent(Intent.ACTION_CALL);
                callIntent4.setData(Uri.parse("tel:105"));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                                new String[]{Manifest.permission
                                        .WRITE_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE},
                                MULTIPLE_PERMISSIONS);
                        return;
                    }
                    else startActivity(callIntent4);
                }*/
                break;

            /*case R.id.btnCorreo:
                String[] TO = {"infosalud@minsa.gob.pe"};
                String[] CC = {""};
                Intent emailIntent = new Intent(Intent.ACTION_SEND);

                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                emailIntent.putExtra(Intent.EXTRA_CC, CC);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Posible infectado");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Hola...");

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                    finish();
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(this, "Error al cargar la información", Toast.LENGTH_SHORT).show();
                }
                break;*/

        }
    }
}
