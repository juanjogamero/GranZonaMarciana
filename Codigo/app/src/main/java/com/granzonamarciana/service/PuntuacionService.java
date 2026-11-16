package com.granzonamarciana.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import com.granzonamarciana.dao.PuntuacionDao;
import com.granzonamarciana.database.DatabaseExecutor;
import com.granzonamarciana.database.DatabaseHelper;
import com.granzonamarciana.entity.Actor;
import com.granzonamarciana.entity.Gala;
import com.granzonamarciana.entity.Puntuacion;
import com.granzonamarciana.entity.TipoRol;
import com.granzonamarciana.rules.VotacionRules;
import java.time.LocalDateTime;
import java.util.List;

public class PuntuacionService {
    public interface Resultado { void completado(boolean correcto, String mensaje); }
    private final DatabaseHelper db;
    private final PuntuacionDao dao;
    private final Handler main = new Handler(Looper.getMainLooper());

    public PuntuacionService(Context context) {
        this.db=DatabaseHelper.getInstance(context);
        this.dao=db.puntuacionDao();
    }

    /** Inserción verificada en segundo plano; índice único en SQLite contra duplicados. */
    public void registrarVoto(int espectadorId,int galaId,int concursanteId,int valor,Resultado resultado) {
        DatabaseExecutor.execute(() -> {
            boolean ok=false;
            String mensaje;
            try {
                if(!VotacionRules.puntuacionValida(valor)) {
                    mensaje="La puntuación debe estar entre 1 y 10";
                } else {
                    Actor espectador=db.actorDao().buscarPorIdSync(espectadorId);
                    Actor concursante=db.actorDao().buscarPorIdSync(concursanteId);
                    Gala gala=db.galaDao().buscarGalaSync(galaId);
                    if(espectador==null||espectador.getRol()!=TipoRol.ESPECTADOR) {
                        mensaje="Inicia sesión como espectador para votar";
                    } else if(concursante==null||concursante.getRol()!=TipoRol.CONCURSANTE) {
                        mensaje="El usuario seleccionado no es concursante";
                    } else if(gala==null||!VotacionRules.galaVotable(gala.getFechaRealizacion(),LocalDateTime.now())) {
                        mensaje="La gala no está en su ventana de votación de 24 horas";
                    } else {
                        long id=dao.insertarPuntuacion(new Puntuacion(valor,espectadorId,concursanteId,galaId));
                        ok=id!=-1;
                        mensaje=ok?"Tu puntuación se ha registrado":"Ya has votado a este concursante en esta gala";
                    }
                }
            } catch(Exception e) {
                mensaje="No se pudo guardar el voto. Comprueba los datos e inténtalo de nuevo";
            }
            final boolean saved=ok;
            final String response=mensaje;
            main.post(()->resultado.completado(saved,response));
        });
    }

    public LiveData<List<Puntuacion>> listarPuntuaciones(){return dao.listarPuntuaciones();}
    public LiveData<List<Puntuacion>> listarPuntuacionesPorEspectador(int id){return dao.listarPuntuacionesPorEspectador(id);}
    public LiveData<Puntuacion> buscarVoto(int idEspectador,int idGala,int idConcursante){
        return dao.buscarVoto(idEspectador,idGala,idConcursante);
    }
    public LiveData<Float> obtenerMedia(int idConcursante,int idGala){
        return dao.obtenerMediaPuntuacion(idConcursante,idGala);
    }
    public LiveData<List<Puntuacion>> listarPuntuacionesPorGala(int idGala){
        return dao.listarPuntuacionesPorGala(idGala);
    }
}
