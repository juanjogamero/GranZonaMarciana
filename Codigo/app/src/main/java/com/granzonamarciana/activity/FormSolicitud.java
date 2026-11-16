package com.granzonamarciana.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.granzonamarciana.R;
import com.granzonamarciana.entity.EstadoSolicitud;
import com.granzonamarciana.entity.TipoRol;
import com.granzonamarciana.service.ActorService;
import com.granzonamarciana.service.SolicitudService;

public class FormSolicitud extends AppCompatActivity {
    private SolicitudService service;
    private ActorService actores;
    private TextInputEditText mensaje;
    private MaterialButton enviar,aprobar,rechazar;
    private int edicionId,solicitudId,usuarioId;
    private boolean modoAdmin;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_form_solicitud);
        service=new SolicitudService(this);
        actores=new ActorService(this);
        edicionId=getIntent().getIntExtra("idEdicion",-1);
        solicitudId=getIntent().getIntExtra("idSolicitud",-1);
        usuarioId=getSharedPreferences("granzonaUser",MODE_PRIVATE).getInt("id",-1);
        String rol=getSharedPreferences("granzonaUser",MODE_PRIVATE).getString("rol","");
        modoAdmin=solicitudId!=-1;
        mensaje=findViewById(R.id.etDescripcionInscripcion);
        enviar=findViewById(R.id.btnEnviarSolicitud);
        aprobar=findViewById(R.id.btnAprobarSolicitud);
        rechazar=findViewById(R.id.btnRechazarSolicitud);
        TextView instrucciones=findViewById(R.id.tvInstrucciones);
        LinearLayout adminActions=findViewById(R.id.layoutAccionesAdmin);
        if(modoAdmin) {
            if(!TipoRol.ADMINISTRADOR.name().equals(rol)){finish();return;}
            enviar.setVisibility(View.GONE);
            adminActions.setVisibility(View.VISIBLE);
            mensaje.setEnabled(false);
            service.buscarSolicitudPorId(solicitudId).observe(this,s->{
                if(s==null){instrucciones.setText("Solicitud no encontrada");aprobar.setEnabled(false);rechazar.setEnabled(false);return;}
                mensaje.setText(s.getMensaje());
                boolean pendiente=s.getEstado()==EstadoSolicitud.PENDIENTE;
                aprobar.setEnabled(pendiente);
                rechazar.setEnabled(pendiente);
                actores.buscarPorId(s.getIdConcursante()).observe(this,a->{
                    if(a!=null)instrucciones.setText("Solicitante: "+a.getNombre()+" "+a.getApellido1()+" · "+s.getEstado());
                });
            });
        }else{
            if(edicionId<=0||usuarioId<=0||!TipoRol.ESPECTADOR.name().equals(rol)){
                Toast.makeText(this,"Debes ser espectador para solicitar plaza",Toast.LENGTH_LONG).show();
                finish();return;
            }
            enviar.setVisibility(View.VISIBLE);
            adminActions.setVisibility(View.GONE);
        }
        enviar.setOnClickListener(v->{
            String texto=mensaje.getText()==null?"":mensaje.getText().toString().trim();
            if(texto.isEmpty()){mensaje.setError("Escribe tu motivación");return;}
            enviar.setEnabled(false);
            service.crear(usuarioId,edicionId,texto,(ok,detalle)->resultado(ok,detalle,enviar));
        });
        aprobar.setOnClickListener(v->resolver(EstadoSolicitud.ACEPTADA));
        rechazar.setOnClickListener(v->resolver(EstadoSolicitud.RECHAZADA));
        findViewById(R.id.btnCancelarSolicitud).setOnClickListener(v->finish());
    }

    private void resolver(EstadoSolicitud estado) {
        aprobar.setEnabled(false);rechazar.setEnabled(false);
        service.resolver(solicitudId,estado,(ok,detalle)->{
            if(isFinishing()||isDestroyed())return;
            Toast.makeText(this,detalle,Toast.LENGTH_LONG).show();
            if(ok)finish();else{aprobar.setEnabled(true);rechazar.setEnabled(true);}
        });
    }
    private void resultado(boolean ok,String detalle,MaterialButton boton){
        if(isFinishing()||isDestroyed())return;
        Toast.makeText(this,detalle,Toast.LENGTH_LONG).show();
        if(ok)finish();else boton.setEnabled(true);
    }
}
