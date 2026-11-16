package com.granzonamarciana;
import static org.junit.Assert.*;
import org.junit.Test;
import java.time.LocalDateTime;
import com.granzonamarciana.rules.VotacionRules;
public class VotacionRulesTest {
    private final LocalDateTime inicio=LocalDateTime.of(2026,11,16,22,0);
    @Test public void aceptaValoresEntreUnoYDiez(){
        assertFalse(VotacionRules.puntuacionValida(0));
        assertTrue(VotacionRules.puntuacionValida(1));
        assertTrue(VotacionRules.puntuacionValida(10));
        assertFalse(VotacionRules.puntuacionValida(11));
    }
    @Test public void controlaVentanaDeVeinticuatroHoras(){
        assertFalse(VotacionRules.galaVotable(inicio,inicio.minusSeconds(1)));
        assertTrue(VotacionRules.galaVotable(inicio,inicio));
        assertTrue(VotacionRules.galaVotable(inicio,inicio.plusHours(23)));
        assertFalse(VotacionRules.galaVotable(inicio,inicio.plusHours(24)));
    }
}
