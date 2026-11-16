package com.granzonamarciana.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import com.granzonamarciana.dao.SolicitudDao;
import com.granzonamarciana.database.DatabaseExecutor;
import com.granzonamarciana.database.DatabaseHelper;
import com.granzonamarciana.entity.*;
import com.granzonamarciana.rules.SolicitudRules;
import java.time.LocalDate;
import java.util.List;

public class SolicitudService {
    public interface Resultado { void completado(boolean correcto,String mensaje); }
    private final DatabaseHelper db;
    private final SolicitudDao dao;
    private final Handler main=new Handler(Looper.getMainLooper());

    public SolicitudService(Context context){
        db=DatabaseHelper.getInstance(context);
        dao=db.solicitudDao();
    }

    public void crear(int usuarioId,int edicionId,String texto,Resultado resultado){
        DatabaseExecutor.execute(()->{
            final String[] message={""};
            final boolean[] ok={false};
            try{
                db.runInTransaction(()->{
                    Actor actor=db.actorDao().buscarPorIdSync(usuarioId);
                    Edicion edicion=db.edicionDao().buscarEdicionSync(edicionId);
                    if(actor==null||actor.getRol()!=TipoRol.ESPECTADOR){
                        message[0]="Debes iniciar sesión como espectador";
                    } else if(edicion==null||edicion.getFechaFin()==null||
                            edicion.getFechaFin().isBefore(LocalDate.now())) {
                        message[0]="La edición ya no admite solicitudes";
                    } else if(texto==null||texto.trim().isEmpty()){
                        message[0]="Escribe por qué quieres participar";
                    } else if(dao.existeSolicitud(usuarioId,edicionId)>0){
                        message[0]="Ya existe una solicitud de ese espectador para esta edición";
                    } else {
                        long id=dao.insertarSolicitud(new Solicitud(texto.trim(),EstadoSolicitud.PENDIENTE,usuarioId,edicionId));
                        ok[0]=id!=-1;
                        message[0]=ok[0]?"Solicitud presentada correctamente":"Ya existe una solicitud para esta edición";
                    }
                });
            }catch(Exception ex){message[0]="No se pudo registrar la solicitud";}
            main.post(()->resultado.completado(ok[0],message[0]));
        });
    }

    public void resolver(int solicitudId,EstadoSolicitud nuevo,Resultado resultado){
        DatabaseExecutor.execute(()->{
            final boolean[] ok={false};
            final String[] message={"No se pudo gestionar la solicitud"};
            try{
                db.runInTransaction(()->{
                    Solicitud s=dao.buscarSolicitudSync(solicitudId);
                    if(s==null||!SolicitudRules.puedeResolver(s.getEstado())){
                        message[0]="La solicitud no existe o ya fue resuelta";
                        return;
                    }
                    if(nuevo==EstadoSolicitud.ACEPTADA){
                        Edicion edicion=db.edicionDao().buscarEdicionSync(s.getIdEdicion());
                        Actor actor=db.actorDao().buscarPorIdSync(s.getIdConcursante());
                        if(edicion==null||actor==null){
                            message[0]="No se encontró la edición o el aspirante";
                            return;
                        }
                        int aceptadas=dao.contarAceptadas(edicion.getId());
                        if(!SolicitudRules.permiteAprobar(s.getEstado(),aceptadas,edicion.getMaxParticipantes())){
                            message[0]="No quedan plazas disponibles en esta edición";
                            return;
                        }
                        s.setEstado(EstadoSolicitud.ACEPTADA);
                        dao.actualizarSolicitud(s);
                        db.actorDao().actualizarRol(actor.getId(),TipoRol.CONCURSANTE.name());
                        int pendientesRechazadas=0;
                        if(aceptadas+1>=edicion.getMaxParticipantes()){
                            pendientesRechazadas=dao.rechazarPendientes(edicion.getId());
                        }
                        message[0]="Solicitud aprobada"+(pendientesRechazadas>0?
                            ". Se han rechazado "+pendientesRechazadas+" solicitudes por aforo completo":"");
                    }else if(nuevo==EstadoSolicitud.RECHAZADA){
                        s.setEstado(EstadoSolicitud.RECHAZADA);
                        dao.actualizarSolicitud(s);
                        message[0]="Solicitud rechazada";
                    }else{
                        message[0]="Estado de solicitud no válido";
                        return;
                    }
                    ok[0]=true;
                });
            }catch(Exception ex){message[0]="No se pudo actualizar la solicitud";}
            main.post(()->resultado.completado(ok[0],message[0]));
        });
    }

    public LiveData<List<Solicitud>> listarSolicitudes(){return dao.listarSolicitudes();}
    public LiveData<Solicitud> buscarSolicitudPorId(int id){return dao.buscarSolicitudPorId(id);}
    public LiveData<List<Solicitud>> listarSolicitudesPorEdicion(int id){return dao.listarSolicitudesPorEdicion(id);}
    public LiveData<Solicitud> buscarSolicitudDeUsuario(int id,int edicion){return dao.buscarSolicitudDeUsuario(id,edicion);}
    public LiveData<List<Solicitud>> listarSolicitudesPendientes(){return dao.listarSolicitudesPendientes();}
}
