package com.example.ussdapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicator;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityResultLauncher<String> callPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted->{});
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE);
        }

        TextView airtimeBalanceBtn = findViewById(R.id.airtimeBalance);
        airtimeBalanceBtn.setOnClickListener(view -> runSimpleUSSDCode("*131#"));

        TextView bundleBalanceBtn = findViewById(R.id.bundleBalance);
        bundleBalanceBtn.setOnClickListener(view -> runSimpleUSSDCode("*154*5#"));

        TextView mobileMoneyBtn = findViewById(R.id.MobileMoney);
        mobileMoneyBtn.setOnClickListener(view -> runSimpleUSSDCode("*182*6*1#"));

        TextView openCustomCodeDialogBtn = findViewById(R.id.openCuster0inlogBtn);
        openCustomCodeDialogBtn.setOnClickListener(view -> openCustomUSSDCodeDialog());
    }

        private void openCustomUSSDCodeDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.custom_code, null);
        EditText codeBtn = view.findViewById(R.id.codeBtn);
        TextView runBtn = view.findViewById(R.id.runBtn);
        TextView cancelBtn = view.findViewById(R.id.cancelBtn);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setView(view);
        final AlertDialog dialog = alertBuilder.show();

        cancelBtn.setOnClickListener(view1 -> dialog.dismiss());

        runBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String USSDCode = codeBtn.getText().toString();

                if (!USSDCode.startsWith("*") && !USSDCode.endsWith("#")){
                    Toast.makeText(getApplicationContext(), "Enter a valid USSD Code", Toast.LENGTH_SHORT).show();
                return;
                }

                runMulOpUSSDCode(USSDCode);
                dialog.dismiss();
            }
        });
    }
    private void runMulOpUSSDCode(String USSDCode) {
        USSDCode = USSDCode.substring(0, USSDCode.length()-1);
        USSDCode = USSDCode + Uri.encode("#");
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel"+USSDCode));
        startActivity(intent);
    }



    private void runSimpleUSSDCode(String USSDCode) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE){
                TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

                View view = LayoutInflater.from(this).inflate(R.layout.status_dialog, null);
                CircularProgressIndicator progressIndicator = view.findViewById(R.id.progressIndicator);
                TextView messageView = view.findViewById(R.id.message);
                TextView okBtn = view.findViewById(R.id.okBtn);

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setView(view);
                alertBuilder.setCancelable(false);
                final AlertDialog dialog = alertBuilder.show();

                okBtn.setVisibility(View.GONE);
                okBtn.setOnClickListener(view1 -> dialog.dismiss());

                manager.sendUssdRequest(USSDCode, new TelephonyManager.UssdResponseCallback() {
                    @Override
                    public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                        super.onReceiveUssdResponse(telephonyManager, request, response);

                        progressIndicator.setVisibility(View.GONE);
                        messageView.setText(response.toString());
                        okBtn.setVisibility(View.VISIBLE);
                    }

                    @SuppressLint("NewApi")
                    @Override
                    public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failureCode) {
                        super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode);

                        progressIndicator.setVisibility(View.GONE);
                        messageView.setText("Failed, Please Try Again Later");
                        okBtn.setVisibility(View.VISIBLE);
                    }
                }, new Handler());
            }else {
                USSDCode = USSDCode.substring(0, USSDCode.length()-1);
                USSDCode = USSDCode + Uri.encode("#");
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel"+USSDCode));
                startActivity(intent);
            }
        }
    }
}