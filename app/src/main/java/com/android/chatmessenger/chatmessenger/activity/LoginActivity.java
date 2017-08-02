package com.android.chatmessenger.chatmessenger.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.chatmessenger.chatmessenger.R;
import com.android.chatmessenger.chatmessenger.helper.Permissao;
import com.android.chatmessenger.chatmessenger.helper.Preferencias;
import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;

import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextTelefone;
    private EditText editTextDDI;
    private EditText editTextDDD;
    private Button buttonCadastrar;
    private EditText editTextNome;
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.SEND_SMS,
            Manifest.permission.INTERNET

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Permissao.validaPermissoes(1, this, permissoesNecessarias);

        editTextTelefone = (EditText) findViewById(R.id.editTextTel_Id);
        editTextDDI = (EditText) findViewById(R.id.editTextDDI_Id);
        editTextDDD = (EditText) findViewById(R.id.editTextDDD_Id);
        editTextNome = (EditText) findViewById(R.id.editTextNome_Id);

        buttonCadastrar = (Button) findViewById(R.id.buttonCadastrar_Id);

        //mascara telefone
        SimpleMaskFormatter simpleMaskTelefone = new SimpleMaskFormatter("NNNNN-NNNN");
        MaskTextWatcher maskTextTelefone = new MaskTextWatcher(editTextTelefone, simpleMaskTelefone);
        editTextTelefone.addTextChangedListener(maskTextTelefone);

        //Mascara DDI
        SimpleMaskFormatter simpleMaskDDI = new SimpleMaskFormatter("+NN");
        MaskTextWatcher maskTextDDI = new MaskTextWatcher(editTextDDI, simpleMaskDDI);
        editTextDDI.addTextChangedListener(maskTextDDI);

        //Mascara DDD
        SimpleMaskFormatter simpleMaskDDD = new SimpleMaskFormatter("NN");
        MaskTextWatcher maskTextDDD = new MaskTextWatcher(editTextDDD, simpleMaskDDD);
        editTextDDD.addTextChangedListener(maskTextDDD);


        //Evento do butao cadastrar
        buttonCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nomeUsuario = editTextNome.getText().toString();
                String telefoneCompleto =
                                editTextDDI.getText().toString()+
                                editTextDDD.getText().toString()+
                                editTextTelefone.getText().toString();


                String telefoneSemFormatacao;
                telefoneCompleto = telefoneCompleto.replace("+","");
                telefoneSemFormatacao = telefoneCompleto.replace("-", "");

                //Gerar Token
                Random randomico = new Random();
                int numeroRandomico = randomico.nextInt(9999-1000)+1000; //forçado a geração de 4 digitos;

                String token = String.valueOf(numeroRandomico);
                String mensagemEnvio = "Chat Messenger Código de confirmação: " + token;

                //envio do sms

                enviaSMS("+" + telefoneSemFormatacao, mensagemEnvio);
                boolean enviadoSMS = enviaSMS("+" + telefoneSemFormatacao, mensagemEnvio);

                //caso seja enviado a mensagem usario vai para a seguinte activity
                if(enviadoSMS)
                {
                    Intent intent = new Intent(LoginActivity.this, ValidadorActivity.class); // instanciando a activity para ir para a proxima
                    startActivity(intent); //
                    finish(); //destruindo a activity atual.

                }else{
                    Toast.makeText(LoginActivity.this, "Problema ao enviar o SMS tente novamente", Toast.LENGTH_LONG).show();
                }

                //salvando dados para validação
                Preferencias preferencias = new Preferencias(getApplicationContext());

                preferencias.salvarUsuarioPreferencias(nomeUsuario, telefoneSemFormatacao, token);

                HashMap<String, String> usuario = preferencias.getDadosUsuarios(); //Salvando hashmap de preferencias com os dados do usuario no hassmap usuarios.

                Log.i("TOKEN", "T:" + usuario.get("token"));
            }
        });

    }

    //Metodo envio SMS
    private boolean enviaSMS(String telefone, String mensagem){
        try {

            SmsManager smsManager = SmsManager.getDefault(); //recuperando a instacia da classe
            smsManager.sendTextMessage(telefone,null, mensagem, null, null );

            return true;

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //Tratando permissoes negadas
    public void onRequestPermissionsResult(int requestCode, String[] permissoes,int[] grantResults){

        super.onRequestPermissionsResult(requestCode, permissoes, grantResults);

        for (int resultado: grantResults){
            if (resultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissoes negadas");
        builder.setMessage("Para utilizar este app é necessario aceitar as permissoes");

        builder.setPositiveButton("CONFIMAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });


        AlertDialog dialog = builder.create();
        dialog.show();

    }

}
