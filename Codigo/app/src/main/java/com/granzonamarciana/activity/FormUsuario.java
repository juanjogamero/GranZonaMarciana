package com.granzonamarciana.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.granzonamarciana.R;
import com.granzonamarciana.entity.Actor;
import com.granzonamarciana.entity.TipoRol;
import com.granzonamarciana.service.ActorService;
import com.squareup.picasso.Picasso;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;

public class FormUsuario extends AppCompatActivity {

    private ActorService actorService;
    private EditText etNombreUsuario, etContrasena, etNombre, etApellido1, etApellido2, etCorreo, etTelefono, etUrlImagen;
    private AutoCompleteTextView autoCompleteRol;
    private MaterialCardView cardPerfil;
    private ImageView imgPerfil;
    private Actor actorCargado;

    private int idSesion;
    private String rolSesion;
    private int idActorGestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_usuario);

        actorService = new ActorService(this);
        inicializarCampos();

        // 1. Obtener datos de la sesión actual
        idSesion = getSharedPreferences("granzonaUser", MODE_PRIVATE).getInt("id", -1);
        rolSesion = getSharedPreferences("granzonaUser", MODE_PRIVATE).getString("rol", "");

        // 2. Comprobar si venimos de la lista de usuarios (Gestión de Admin)
        idActorGestion = getIntent().getIntExtra("id_actor_gestion", -1);

        configurarSelectorRoles();

        // 3. Lógica de Carga de Datos
        if (idActorGestion != -1 && !TipoRol.ADMINISTRADOR.name().equals(rolSesion)) {
            Toast.makeText(this,"Acceso no autorizado",Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (idActorGestion != -1) {
            // Caso: El Admin está consultando a otro usuario
            mostrarFotoPerfil(true);
            cargarUsuarioParaFormulario(idActorGestion);
        } else if (idSesion != -1) {
            // Caso: El usuario está editando su propio perfil (Mi Perfil)
            mostrarFotoPerfil(true);
            cargarUsuarioParaFormulario(idSesion);
            // Al editar el perfil propio, bloqueamos el cambio de rol por seguridad
            autoCompleteRol.setEnabled(false);
        } else {
            // Caso: Registro de nuevo usuario
            mostrarFotoPerfil(false);
            TextView tvEncabezado = findViewById(R.id.tvEncabezado);
            tvEncabezado.setText("Registro de Usuario");
        }

        // Listener para actualizar imagen cuando cambie la URL (solo si la foto está visible)
        etUrlImagen.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && cardPerfil.getVisibility() == View.VISIBLE) {
                cargarImagenDesdeUrl();
            }
        });

        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());
        findViewById(R.id.btnGuardar).setOnClickListener(v -> procesarFormulario());
    }

    private void inicializarCampos() {
        etNombreUsuario = findViewById(R.id.etNombreUsuario);
        etContrasena = findViewById(R.id.etContrasena);
        etNombre = findViewById(R.id.etNombre);
        etApellido1 = findViewById(R.id.etApellido1);
        etApellido2 = findViewById(R.id.etApellido2);
        etCorreo = findViewById(R.id.etCorreo);
        etTelefono = findViewById(R.id.etTelefono);
        etUrlImagen = findViewById(R.id.etUrlImagen);
        autoCompleteRol = findViewById(R.id.autoCompleteRol);
        cardPerfil = findViewById(R.id.cardPerfil);
        imgPerfil = findViewById(R.id.imgPerfil);
    }

    private void mostrarFotoPerfil(boolean mostrar) {
        cardPerfil.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }

    private void configurarSelectorRoles() {
        List<String> roles = new ArrayList<>();
        // Por defecto, cualquier actor no autenticado puede ser Espectador o Concursante
        roles.add(TipoRol.ESPECTADOR.toString());
        // Los nuevos participantes solicitan acceso como espectadores; el admin aprueba.

        // Solo un Administrador puede asignar el rol de Administrador
        if (rolSesion.equals(TipoRol.ADMINISTRADOR.toString())) {
            roles.add(TipoRol.CONCURSANTE.toString());
            roles.add(TipoRol.ADMINISTRADOR.toString());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, roles);
        autoCompleteRol.setAdapter(adapter);
    }

    private void cargarUsuarioParaFormulario(int id) {
        actorService.buscarPorId(id).observe(this, actor -> {
            if (actor != null) {
                actorCargado = actor;
                etNombreUsuario.setText(actor.getUsername());
                etNombre.setText(actor.getNombre());
                etApellido1.setText(actor.getApellido1());
                etApellido2.setText(actor.getApellido2());
                etCorreo.setText(actor.getCorreo());
                etTelefono.setText(actor.getTelefono());
                etUrlImagen.setText(actor.getUrlImagen());
                autoCompleteRol.setText(actor.getRol().toString(), false);

                // Cargar imagen de perfil
                cargarImagenDesdeUrl();
            }
        });
    }

    private void cargarImagenDesdeUrl() {
        String url = etUrlImagen.getText().toString().trim();

        if (!TextUtils.isEmpty(url)) {
            Picasso.get()
                    .load(url)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(android.R.drawable.ic_menu_report_image)
                    .resize(200, 200)
                    .centerCrop()
                    .into(imgPerfil);
        } else {
            imgPerfil.setImageResource(android.R.drawable.ic_menu_report_image);
        }
    }

    private void procesarFormulario() {
        String username = etNombreUsuario.getText().toString().trim();
        String passRaw = etContrasena.getText().toString().trim();
        String rolSeleccionado = autoCompleteRol.getText().toString();
        if(actorCargado != null && idActorGestion == -1) {
            // Editar el perfil propio nunca puede elevar o reducir el rol.
            rolSeleccionado=actorCargado.getRol().name();
        }
        if(actorCargado == null && !TipoRol.ADMINISTRADOR.name().equals(rolSesion)) {
            rolSeleccionado=TipoRol.ESPECTADOR.name();
        }

        if (!username.matches("[a-zA-Z0-9_.-]{3,30}")) {
            etNombreUsuario.setError("Usuario de 3 a 30 caracteres sin espacios");
            return;
        }
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(rolSeleccionado)) {
            Toast.makeText(this, "Usuario y Rol son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si es edición y no se escribe pass, mantenemos la anterior. Si es nuevo, ciframos.
        String passwordFinal;
        if (actorCargado != null && TextUtils.isEmpty(passRaw)) {
            passwordFinal = actorCargado.getPassword();
        } else if (!TextUtils.isEmpty(passRaw) && passRaw.length() >= 8) {
            passwordFinal = BCrypt.hashpw(passRaw, BCrypt.gensalt());
        } else {
            Toast.makeText(this, "La contraseña debe tener 8 caracteres como mínimo", Toast.LENGTH_SHORT).show();
            return;
        }


        boolean editando=actorCargado!=null;
        Actor destino;
        if(editando){
            destino=actorCargado;
            destino.setUsername(username);
            destino.setPassword(passwordFinal);
            destino.setNombre(etNombre.getText().toString().trim());
            destino.setApellido1(etApellido1.getText().toString().trim());
            destino.setApellido2(etApellido2.getText().toString().trim());
            destino.setCorreo(etCorreo.getText().toString().trim());
            destino.setTelefono(etTelefono.getText().toString().trim());
            destino.setUrlImagen(etUrlImagen.getText().toString().trim());
            destino.setRol(TipoRol.valueOf(rolSeleccionado));
        }else{
            destino=new Actor(username,passwordFinal,TipoRol.valueOf(rolSeleccionado),
                etNombre.getText().toString().trim(),etApellido1.getText().toString().trim(),
                etApellido2.getText().toString().trim(),etCorreo.getText().toString().trim(),
                etTelefono.getText().toString().trim(),etUrlImagen.getText().toString().trim());
        }
        View guardar=findViewById(R.id.btnGuardar);
        guardar.setEnabled(false);
        actorService.guardarActor(destino,editando,(ok,detalle)->{
            if(isFinishing()||isDestroyed())return;
            Toast.makeText(this,detalle,Toast.LENGTH_LONG).show();
            if(ok){
                if(editando&&idSesion==destino.getId()){
                    getSharedPreferences("granzonaUser",MODE_PRIVATE).edit()
                        .putString("username",destino.getUsername())
                        .putString("rol",destino.getRol().name()).apply();
                }
                finish();
            }else guardar.setEnabled(true);
        });
    }
}
