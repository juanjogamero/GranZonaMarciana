package com.granzonamarciana.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import com.granzonamarciana.dao.ActorDao;
import com.granzonamarciana.database.DatabaseExecutor;
import com.granzonamarciana.database.DatabaseHelper;
import com.granzonamarciana.entity.Actor;
import java.util.List;

public class ActorService {
    public interface Resultado { void completado(boolean correcto,String mensaje); }
    private final ActorDao dao;
    private final Handler main=new Handler(Looper.getMainLooper());

    public ActorService(Context context) {
        dao=DatabaseHelper.getInstance(context).actorDao();
    }
    public void insertarActor(Actor actor) {
        DatabaseExecutor.execute(()->dao.insertarActor(actor));
    }
    public void actualizarActor(Actor actor) {
        DatabaseExecutor.execute(()->dao.actualizarActor(actor));
    }
    public void eliminarActor(Actor actor) {
        DatabaseExecutor.execute(()->dao.eliminarActor(actor));
    }

    /** Confirmar el resultado solo después de terminar la escritura en Room. */
    public void guardarActor(Actor actor,boolean edicion,Resultado callback) {
        DatabaseExecutor.execute(()->{
            boolean correcto=true;
            String mensaje=edicion?"Perfil actualizado correctamente":"Cuenta registrada correctamente";
            try {
                if(edicion)dao.actualizarActor(actor);
                else dao.insertarActor(actor);
            } catch(android.database.sqlite.SQLiteConstraintException e){
                correcto=false;
                mensaje="Este nombre de usuario ya está registrado";
            } catch(Exception e){
                correcto=false;
                mensaje="No se pudo guardar. Comprueba los datos e inténtalo otra vez";
            }
            final boolean ok=correcto;
            final String feedback=mensaje;
            main.post(()->callback.completado(ok,feedback));
        });
    }

    public LiveData<List<Actor>> listarUsuariosSistema(){return dao.listarUsuariosSistema();}
    public LiveData<Actor> buscarPorId(int id){return dao.buscarPorId(id);}
    public LiveData<Actor> buscarPorUsername(String username){return dao.buscarPorUsername(username);}
    public LiveData<List<Actor>> listarTodos(){return dao.listarTodos();}
    public Actor loginSync(String username){return dao.loginSync(username);}
}
