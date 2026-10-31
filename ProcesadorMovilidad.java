import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ProcesadorMovilidad {

    // a) Afluencia por estación: solo cuenta los eventos de "entrada".
    public Map<String, Long> calcularAfluencia(List<RegistroTransito> registros) {
        return registros.stream()
                .filter(r -> r.accion().equalsIgnoreCase("entrada"))
                .collect(Collectors.groupingBy(
                        RegistroTransito::estacion,
                        Collectors.counting()));
    }

    // a) Variante paralela: mismo resultado, procesada con parallelStream().
    public Map<String, Long> calcularAfluenciaParalela(List<RegistroTransito> registros) {
        return registros.parallelStream()
                .filter(r -> r.accion().equalsIgnoreCase("entrada"))
                .collect(Collectors.groupingBy(
                        RegistroTransito::estacion,
                        Collectors.counting()));
    }

    // b) Horas pico: registros agrupados por hora y ordenados de mayor a menor flujo.
    public List<Map.Entry<Integer, Long>> identificarHorasPico(List<RegistroTransito> registros) {
        return registros.stream()
                .collect(Collectors.groupingBy(
                        r -> r.timestamp().getHour(),
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .toList();
    }

    // c) Rutas más utilizadas, de mayor a menor volumen.
    public List<Map.Entry<String, Long>> rutasMasUtilizadas(List<RegistroTransito> registros) {
        return registros.stream()
                .collect(Collectors.groupingBy(
                        RegistroTransito::ruta,
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .toList();
    }

    // d) Patrones de viaje: estaciones por usuario en orden cronológico.
    public Map<String, List<String>> obtenerPatronesDeViaje(List<RegistroTransito> registros) {
        return registros.stream()
                .sorted(Comparator.comparing(RegistroTransito::timestamp))
                .collect(Collectors.groupingBy(
                        RegistroTransito::idUsuario,
                        Collectors.mapping(
                                RegistroTransito::estacion,
                                Collectors.toUnmodifiableList())));
    }

    // e) Tiempo promedio (minutos) entre registros consecutivos de un mismo usuario.
    public double tiempoPromedioEntreEstaciones(List<RegistroTransito> registros) {
        Map<String, List<RegistroTransito>> viajesPorUsuario = registros.stream()
                .sorted(Comparator.comparing(RegistroTransito::timestamp))
                .collect(Collectors.groupingBy(RegistroTransito::idUsuario));

        return viajesPorUsuario.values().stream()
                .flatMap(viajes -> IntStream.range(0, viajes.size() - 1)
                        .mapToObj(i -> Duration.between(
                                viajes.get(i).timestamp(),
                                viajes.get(i + 1).timestamp()).toMinutes()))
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
    }

    // f) Rutas críticas: las que alcanzan o superan el umbral recibido por parámetro.
    public List<String> detectarSobrecarga(List<RegistroTransito> registros, long umbralCritico) {
        return registros.stream()
                .collect(Collectors.groupingBy(RegistroTransito::ruta, Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() >= umbralCritico)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
    }
}