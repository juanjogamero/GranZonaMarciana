package com.granzonamarciana.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.granzonamarciana.entity.Solicitud;

import java.util.List;

@Dao
public interface SolicitudDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertarSolicitud(Solicitud solicitud);

    @Update
    void actualizarSolicitud(Solicitud solicitud);

    @Delete
    void eliminarSolicitud(Solicitud solicitud);

    @Query("SELECT * FROM solicitud WHERE id = :id LIMIT 1")
    Solicitud buscarSolicitudSync(int id);

    @Query("SELECT COUNT(*) FROM solicitud WHERE idConcursante = :usuario AND idEdicion = :edicion")
    int existeSolicitud(int usuario,int edicion);

    @Query("SELECT COUNT(*) FROM solicitud WHERE idEdicion = :edicion AND estado = 'ACEPTADA'")
    int contarAceptadas(int edicion);

    @Query("UPDATE solicitud SET estado = 'RECHAZADA' WHERE idEdicion = :edicion AND estado = 'PENDIENTE'")
    int rechazarPendientes(int edicion);

    // Ver todas las solicitudes
    @Query("SELECT * FROM solicitud")
    LiveData<List<Solicitud>> listarSolicitudes();

    // Ver solicitud por id
    @Query("SELECT * FROM solicitud WHERE id = :id")
    LiveData<Solicitud> buscarSolicitudPorId(int id);

    // Ver las solicitudes de una edición específica
    @Query("SELECT * FROM solicitud WHERE idEdicion = :idEdicion")
    LiveData<List<Solicitud>> listarSolicitudesPorEdicion(int idEdicion);

    // Ver si un usuario ya tiene solicitud en esa edición
    @Query("SELECT * FROM solicitud WHERE idConcursante = :idUsuario AND idEdicion = :idEdicion")
    LiveData<Solicitud> buscarSolicitudDeUsuario(int idUsuario, int idEdicion);

    // Listar solo las pendientes
    @Query("SELECT * FROM solicitud WHERE estado = 'PENDIENTE'")
    LiveData<List<Solicitud>> listarSolicitudesPendientes();
}
