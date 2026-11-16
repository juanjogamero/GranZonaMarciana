package com.granzonamarciana.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.OnConflictStrategy;

import com.granzonamarciana.entity.Actor;

import java.util.List;

@Dao
public interface ActorDao {

    // Insertar un Actor
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertarActor(Actor actor);

    // Actualizar un Actor
    @Update
    void actualizarActor(Actor actor);

    // Eliminar un actor
    @Delete
    void eliminarActor(Actor actor);

    // Para realizar el Login y buscar perfiles por nombre de usuario
    @Query("SELECT * FROM actor WHERE username = :username")
    LiveData<Actor> buscarPorUsername(String username);

    // Buscar por ID para cargar datos del perfil tras el login
    @Query("SELECT * FROM actor WHERE id = :id")
    LiveData<Actor> buscarPorId(int id);

    // Para que el Administrador pueda listar a todos los concursantes y espectadores
    // Excluimos a otros administradores del listado general de usuarios si es necesario
    @Query("SELECT * FROM actor WHERE rol != 'ADMINISTRADOR'")
    LiveData<List<Actor>> listarUsuariosSistema();

    // Listar actores específicos por rol
    @Query("SELECT * FROM actor WHERE rol = :rol")
    LiveData<List<Actor>> listarActoresPorRol(String rol);

    // Listar absolutamente todos
    @Query("SELECT * FROM actor")
    LiveData<List<Actor>> listarTodos();

    @Query("SELECT COUNT(*) FROM actor")
    int contarActores();

    @Query("SELECT * FROM actor WHERE id = :id LIMIT 1")
    Actor buscarPorIdSync(int id);

    @Query("UPDATE actor SET rol = :rol WHERE id = :id")
    int actualizarRol(int id, String rol);

    // Método síncrono para comprobaciones en hilos secundarios
    @Query("SELECT * FROM actor WHERE username = :username LIMIT 1")
    Actor loginSync(String username);
}
