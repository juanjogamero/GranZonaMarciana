package com.granzonamarciana.activity;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.granzonamarciana.R;
import com.granzonamarciana.entity.TipoRol;
import com.granzonamarciana.service.PuntuacionService;

public class FormPuntuacion extends AppCompatActivity {
    private PuntuacionService service;
    private int idGala;
    private int idEspectador;
    private MaterialButton guardar;
    private TextInputEditText puntos;
    private TextInputEditText concursante;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_form_puntuacion);
        service=new PuntuacionService(this);
        idGala=getIntent().getIntExtra("idGala",-1);
        idEspectador=getSharedPreferences("granzonaUser",MODE_PRIVATE).getInt("id",-1);
        String rol=getSharedPreferences("granzonaUser",MODE_PRIVATE).getString("rol","");
        if(idGala<=0||idEspectador<=0||!TipoRol.ESPECTADOR.name().equals(rol)){
            Toast.makeText(this,"Solo los espectadores registrados pueden votar",Toast.LENGTH_LONG).show();
            finish(); return;
        }
        puntos=findViewById(R.id.etValorVoto);
        concursante=findViewById(R.id.etIdConcursanteVoto);
        guardar=findViewById(R.id.btnGuardarVoto);
        guardar.setOnClickListener(v->enviar());
    }

    private void enviar(){
        int valor,idConcursante;
        try{
            valor=Integer.parseInt(puntos.getText().toString().trim());
            idConcursante=Integer.parseInt(concursante.getText().toString().trim());
        }catch(Exception e){
            Toast.makeText(this,"Introduce una puntuación y un ID numéricos",Toast.LENGTH_LONG).show();
            return;
        }
        if(valor<1||valor>10){puntos.setError("Debes introducir un valor entre 1 y 10");return;}
        if(idConcursante<=0){concursante.setError("ID inválido");return;}
        guardar.setEnabled(false);
        service.registrarVoto(idEspectador,idGala,idConcursante,valor,(correcto,mensaje)->{
            if(isFinishing()||isDestroyed())return;
            Toast.makeText(this,mensaje,Toast.LENGTH_LONG).show();
            if(correcto)finish();
            else guardar.setEnabled(true);
        });
    }
}
