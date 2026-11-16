package com.granzonamarciana.database;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.granzonamarciana.dao.*;
import com.granzonamarciana.entity.*;

@Database(entities={Actor.class,Edicion.class,Gala.class,Noticia.class,Puntuacion.class,Solicitud.class},
        version=8,exportSchema=false)
@TypeConverters({LocalDateConverter.class,LocalDateTimeConverter.class,EstadoSolicitudConverter.class})
public abstract class DatabaseHelper extends RoomDatabase {
    private static volatile DatabaseHelper instance;
    public static final Migration MIGRATION_7_8=new Migration(7,8) {
        @Override public void migrate(@NonNull SupportSQLiteDatabase db) {
            // Conservar el registro más antiguo en caso de duplicados legados.
            db.execSQL("DELETE FROM puntuacion WHERE id NOT IN (SELECT MIN(id) FROM puntuacion GROUP BY idEspectador,idGala,idConcursante)");
            db.execSQL("DELETE FROM solicitud WHERE id NOT IN (SELECT MIN(id) FROM solicitud GROUP BY idConcursante,idEdicion)");
            // No borrar actores en instalaciones con usernames duplicados: requieren limpieza manual.
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_actor_username ON actor(username)");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_puntuacion_idEspectador_idGala_idConcursante ON puntuacion(idEspectador,idGala,idConcursante)");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_solicitud_idConcursante_idEdicion ON solicitud(idConcursante,idEdicion)");
        }
    };

    public static DatabaseHelper getInstance(Context context) {
        if(instance==null) {
            synchronized(DatabaseHelper.class) {
                if(instance==null) {
                    instance=Room.databaseBuilder(context.getApplicationContext(),DatabaseHelper.class,"granzonamarciana")
                        .addMigrations(MIGRATION_7_8)
                        // Versiones anteriores a 7 de esta práctica carecían de migraciones definidas.
                        .fallbackToDestructiveMigration()
                        .build();
                }
            }
        }
        return instance;
    }
    public abstract ActorDao actorDao();
    public abstract EdicionDao edicionDao();
    public abstract GalaDao galaDao();
    public abstract NoticiaDao noticiaDao();
    public abstract PuntuacionDao puntuacionDao();
    public abstract SolicitudDao solicitudDao();
}
