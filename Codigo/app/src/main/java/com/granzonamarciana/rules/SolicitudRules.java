package com.granzonamarciana.rules;

import com.granzonamarciana.entity.EstadoSolicitud;

/** Reglas para los cupos y las transiciones de solicitud. */
public final class SolicitudRules {
    private SolicitudRules() {}
    public static boolean permiteAprobar(EstadoSolicitud actual, int aceptadas, int maxParticipantes) {
        return actual == EstadoSolicitud.PENDIENTE && maxParticipantes > 0 &&
            aceptadas < maxParticipantes;
    }
    public static boolean puedeResolver(EstadoSolicitud actual) {
        return actual == EstadoSolicitud.PENDIENTE;
    }
}
