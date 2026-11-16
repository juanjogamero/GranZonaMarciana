package com.granzonamarciana;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.granzonamarciana.activity.FormUsuario;
import com.granzonamarciana.activity.Menu;
import com.granzonamarciana.database.DatabaseExecutor;
import com.granzonamarciana.database.DatabaseHelper;
import com.granzonamarciana.database.PopulateDB;
import com.granzonamarciana.entity.Actor;
import com.granzonamarciana.entity.TipoRol;
import org.mindrot.jbcrypt.BCrypt;

public class MainActivity extends AppCompatActivity {
    private EditText usuario,contrasena;
    private Button login;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        usuario=findViewById(R.id.etUsername);
        contrasena=findViewById(R.id.etPassword);
        login=findViewById(R.id.btnLogin);
        login.setEnabled(false);
        login.setOnClickListener(v->iniciarSesion());
        findViewById(R.id.btnCrearUsuario).setOnClickListener(v->
            startActivity(new Intent(this,FormUsuario.class)));
        findViewById(R.id.btnInvitado).setOnClickListener(v->
            startActivity(new Intent(this,Menu.class)));

        new PopulateDB(this).populateIfNeeded();
        // Leer y validar la sesión existente contra la base en vez de confiar
        // únicamente en el rol almacenado en las preferencias del dispositivo.
        SharedPreferences prefs=getSharedPreferences("granzonaUser",MODE_PRIVATE);
        int sessionId=prefs.getInt("id",-1);
        DatabaseExecutor.execute(()->{
            DatabaseHelper db=DatabaseHelper.getInstance(this);
            int cuentas=db.actorDao().contarActores();
            Actor actor=sessionId>0?db.actorDao().buscarPorIdSync(sessionId):null;
            runOnUiThread(()->{
                if(isFinishing()||isDestroyed())return;
                login.setEnabled(true);
                if(cuentas==0){mostrarPrimerInicio();return;}
                if(actor!=null){
                    guardarSesion(actor);
                    abrirMenu();
                }else if(sessionId>0){prefs.edit().clear().apply();}
            });
        });
    }

    private void iniciarSesion(){
        String username=usuario.getText().toString().trim();
        String password=contrasena.getText().toString();
        if(username.isEmpty()||password.isEmpty()){
            Toast.makeText(this,"Introduce usuario y contraseña",Toast.LENGTH_SHORT).show();
            return;
        }
        login.setEnabled(false);
        DatabaseExecutor.execute(()->{
            Actor actor=DatabaseHelper.getInstance(this).actorDao().loginSync(username);
            boolean correcto=false;
            try{correcto=actor!=null&&BCrypt.checkpw(password,actor.getPassword());}
            catch(IllegalArgumentException ignored){}
            final boolean valid=correcto;
            runOnUiThread(()->{
                if(isFinishing()||isDestroyed())return;
                if(valid){guardarSesion(actor);abrirMenu();}
                else{
                    login.setEnabled(true);
                    Toast.makeText(this,"Usuario o contraseña incorrectos",Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void guardarSesion(Actor actor){
        getSharedPreferences("granzonaUser",MODE_PRIVATE).edit()
            .putInt("id",actor.getId())
            .putString("username",actor.getUsername())
            .putString("rol",actor.getRol().name())
            .apply();
    }

    private void abrirMenu(){
        startActivity(new Intent(this,Menu.class));
        finish();
    }

    /** No distribuimos ninguna contraseña administrativa fija con la APK. */
    private void mostrarPrimerInicio(){
        LinearLayout content=new LinearLayout(this);
        content.setPadding(dp(20),dp(8),dp(20),dp(8));
        content.setOrientation(LinearLayout.VERTICAL);
        EditText name=new EditText(this);
        name.setHint("Usuario administrador");
        name.setSingleLine(true);
        EditText pass=new EditText(this);
        pass.setHint("Contraseña (mínimo 10 caracteres)");
        pass.setSingleLine(true);
        pass.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        content.addView(name);
        content.addView(pass);
        AlertDialog dialog=new AlertDialog.Builder(this)
            .setTitle("Configura la primera cuenta")
            .setMessage("Tu base de datos está vacía. Crea una cuenta de administrador para gestionar el reality en este dispositivo.")
            .setView(content)
            .setCancelable(false)
            .setPositiveButton("Crear administrador",null).create();
        dialog.setOnShowListener(ignored->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            String username=name.getText().toString().trim();
            String password=pass.getText().toString();
            if(!username.matches("[a-zA-Z0-9_.-]{3,30}")){
                name.setError("Entre 3 y 30 caracteres, sin espacios");return;
            }
            if(password.length()<10){
                pass.setError("Usa al menos 10 caracteres");return;
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            DatabaseExecutor.execute(()->{
                String error=null;
                try{
                    Actor admin=new Actor(username,BCrypt.hashpw(password,BCrypt.gensalt()),
                        TipoRol.ADMINISTRADOR,"Administrador","Local","",
                        "admin@localhost.invalid","","");
                    DatabaseHelper.getInstance(this).actorDao().insertarActor(admin);
                }catch(Exception e){error="No se ha podido crear la cuenta. Elige otro usuario.";}
                final String outcome=error;
                runOnUiThread(()->{
                    if(isFinishing()||isDestroyed())return;
                    if(outcome==null){
                        dialog.dismiss();
                        usuario.setText(username);
                        contrasena.setText("");
                        Toast.makeText(this,"Cuenta creada. Ya puedes iniciar sesión.",Toast.LENGTH_LONG).show();
                    }else{
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        Toast.makeText(this,outcome,Toast.LENGTH_LONG).show();
                    }
                });
            });
        }));
        dialog.show();
    }
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
}
