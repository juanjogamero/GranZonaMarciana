package com.granzonamarciana.rules;

import java.time.LocalDateTime;

/** Reglas de la ventana de votación, independientes de Android y Room. */
public final class VotacionRules {
    private VotacionRules() {}
    public static boolean puntuacionValida(int puntos) { return puntos >= 1 && puntos <= 10; }
    public static boolean galaVotable(LocalDateTime inicio, LocalDateTime ahora) {
        return inicio != null && ahora != null &&
            !ahora.isBefore(inicio) && ahora.isBefore(inicio.plusHours(24));
    }
}
