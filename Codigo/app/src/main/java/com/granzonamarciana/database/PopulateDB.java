package com.granzonamarciana.database;

import android.content.Context;
import com.granzonamarciana.entity.Noticia;
import java.time.LocalDateTime;

/**
 * Contenido editorial de ejemplo. Nunca introduce contraseñas conocidas ni cuentas
 * privilegiadas. La cuenta inicial se configura por el usuario en el primer inicio.
 */
public class PopulateDB {
    private final Context context;

    public PopulateDB(Context context) {
        this.context = context.getApplicationContext();
    }

    public void populateIfNeeded() {
        DatabaseExecutor.execute(() -> {
            DatabaseHelper db = DatabaseHelper.getInstance(context);
            if (db.noticiaDao().contarNoticias() > 0) return;
            db.noticiaDao().insertarNoticia(new Noticia(
                LocalDateTime.now().minusDays(2),
                "Bienvenidos a Gran Zona Marciana",
                "Comienza una nueva edición de este reality ficticio. Descubre las galas y sigue las novedades.",
                "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1000&q=85"));
            db.noticiaDao().insertarNoticia(new Noticia(
                LocalDateTime.now().minusDays(1),
                "El escenario ya está preparado",
                "Las galas reunirán a los concursantes y los espectadores podrán puntuarlos durante las primeras 24 horas.",
                "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=1000&q=85"));
            db.noticiaDao().insertarNoticia(new Noticia(
                LocalDateTime.now(),
                "Inscripciones para la próxima edición",
                "Consulta las ediciones disponibles y presenta tu solicitud como espectador.",
                "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=1000&q=85"));
        });
    }
}
