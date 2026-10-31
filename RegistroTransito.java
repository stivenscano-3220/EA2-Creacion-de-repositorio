import java.time.LocalDateTime;

// Se utiliza 'record' para garantizar la inmutabilidad absoluta de cada evento del sistema.
public record RegistroTransito(
    String idUsuario,
    String ruta,
    String estacion,
    String accion,
    LocalDateTime timestamp
) {}