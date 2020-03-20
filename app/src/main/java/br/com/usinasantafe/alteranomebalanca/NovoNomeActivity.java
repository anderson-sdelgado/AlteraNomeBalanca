package br.com.usinasantafe.alteranomebalanca;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class NovoNomeActivity extends Activity {

    private EditText editTextNome;
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream out;
    private InputStream in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novo_nome);

        editTextNome = (EditText)  findViewById(R.id.editTextNome);
        Button buttonAltNome = (Button) findViewById(R.id.buttonAltNome);
        Button buttonCancNome = (Button) findViewById(R.id.buttonCancNome);

        device = getIntent().getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        editTextNome.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
            }
            @Override
            public void afterTextChanged(Editable et) {
                String s = et.toString();
                if(!s.equals(s.toUpperCase()))
                {
                    s = s.toUpperCase();
                    editTextNome.setText(s);
                    editTextNome.setSelection(editTextNome.length()); //fix reverse texting
                }
            }
        });

        buttonAltNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!editTextNome.getText().toString().equals("") && (editTextNome.getText().toString().length() < 9)) {

                    try {

                        socket = device.createRfcommSocketToServiceRecord(uuid);
                        socket.connect();

                        in = socket.getInputStream();
                        out = socket.getOutputStream();

                        byte[] bytes = new byte[1024];
                        int length;

                        String msg = "#SETNBT," + editTextNome.getText().toString().trim() + "\r\n";

                        if (out != null) {
                            out.write(msg.getBytes());
                        }

                        String texto = "";
                        boolean rodar = true;

                        while (in != null && rodar) {
                            length = in.read(bytes);
                            String msgRec = new String(bytes, 0, length);
                            Log.i("PPA", "RETORNO = " + msgRec);
                            texto = texto + msgRec;
                            if(texto.contains("ACK,NBT")){
                                rodar = false;
                            }
                        }

                        closeCon();
                        AlertDialog.Builder alerta = new AlertDialog.Builder(NovoNomeActivity.this);
                        alerta.setTitle("ATENÇÃO");
                        alerta.setMessage("O NOME DO EQUIPAMENTO FOI ALTERADO COM SUCESSO. POR FAVOR, REINICIE A BALANÇA!");
                        alerta.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finishAffinity();
                            }
                        });
                        alerta.show();

                    } catch (IOException e) {
                        error();
                    }

                }

            }

        });

        buttonCancNome.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finishAffinity();
            }
        });

    }

    public void closeCon(){
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
        }
    }

    private void error() {
        closeCon();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alerta = new AlertDialog.Builder(NovoNomeActivity.this);
                alerta.setTitle("ATENÇÃO");
                alerta.setMessage("FALHA NA CONEXÃO BLUETOOTH.");
                alerta.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                    }
                });
                alerta.show();
            }
        });
    }

    private void sucesso() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

}
