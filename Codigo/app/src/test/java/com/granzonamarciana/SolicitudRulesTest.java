package com.granzonamarciana;
import static org.junit.Assert.*;
import org.junit.Test;
import com.granzonamarciana.entity.EstadoSolicitud;
import com.granzonamarciana.rules.SolicitudRules;
public class SolicitudRulesTest {
    @Test public void laUltimaPlazaEsValida(){
        assertTrue(SolicitudRules.permiteAprobar(EstadoSolicitud.PENDIENTE,4,5));
        assertFalse(SolicitudRules.permiteAprobar(EstadoSolicitud.PENDIENTE,5,5));
    }
    @Test public void soloSeResuelvenPendientes(){
        assertTrue(SolicitudRules.puedeResolver(EstadoSolicitud.PENDIENTE));
        assertFalse(SolicitudRules.puedeResolver(EstadoSolicitud.ACEPTADA));
        assertFalse(SolicitudRules.puedeResolver(EstadoSolicitud.RECHAZADA));
        assertFalse(SolicitudRules.permiteAprobar(EstadoSolicitud.RECHAZADA,0,5));
        assertFalse(SolicitudRules.permiteAprobar(EstadoSolicitud.PENDIENTE,0,0));
    }
}
