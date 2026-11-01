package cl.fersal.inventario.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class FechaHoraChile {
    private static final ZoneId ZONA_SANTIAGO =
            ZoneId.of("America/Santiago");
    private static final DateTimeFormatter FORMATO_BASE_DATOS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private FechaHoraChile() {
    }

    public static String ahora() {
        return LocalDateTime.now(ZONA_SANTIAGO)
                .format(FORMATO_BASE_DATOS);
    }
}
