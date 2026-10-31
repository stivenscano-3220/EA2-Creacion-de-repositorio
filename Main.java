import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

public class Main {

    // Genera registros simulados de forma determinista (sin estado compartido).
    private static List<RegistroTransito> generarLote(int cantidad) {
        List<String> rutas = List.of("Ruta_A", "Ruta_B", "Ruta_C", "Ruta_D");
        List<String> estaciones = List.of("Estacion_Norte", "Estacion_Centro",
                "Estacion_Sur", "Estacion_Occidente", "Estacion_Oriente");
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 31, 5, 0);

        return IntStream.range(0, cantidad)
                .mapToObj(i -> new RegistroTransito(
                        "U" + (i % 5000),
                        rutas.get((i * 7) % rutas.size()),
                        estaciones.get((i * 3) % estaciones.size()),
                        i % 2 == 0 ? "entrada" : "salida",
                        inicio.plusSeconds((long) i * 3)))
                .toList();
    }
    public static void main(String[] args) {
        // Lote pequeño e inmutable (List.of no permite modificaciones)
        List<RegistroTransito> loteDatos = List.of(
            new RegistroTransito("U001", "Ruta_A", "Estacion_Norte", "entrada",
                LocalDateTime.of(2026, 10, 31, 7, 15)),
            new RegistroTransito("U001", "Ruta_A", "Estacion_Centro", "salida",
                LocalDateTime.of(2026, 10, 31, 7, 45)),
            new RegistroTransito("U002", "Ruta_B", "Estacion_Sur", "entrada",
                LocalDateTime.of(2026, 10, 31, 8, 0)),
            new RegistroTransito("U003", "Ruta_A", "Estacion_Norte", "entrada",
                LocalDateTime.of(2026, 10, 31, 7, 20)),
            new RegistroTransito("U002", "Ruta_B", "Estacion_Occidente", "salida",
                LocalDateTime.of(2026, 10, 31, 8, 30))
        );

        ProcesadorMovilidad procesador = new ProcesadorMovilidad();

        System.out.println("=== LOTE PEQUEÑO (5 registros) ===");

        System.out.println("\n--- a) Afluencia por Estación ---");
        procesador.calcularAfluencia(loteDatos).forEach((est, n) -> System.out.println(est + ": " + n));

        System.out.println("\n--- b) Horas Pico ---");
        procesador.identificarHorasPico(loteDatos)
                .forEach(e -> System.out.println(e.getKey() + "h: " + e.getValue()));

        System.out.println("\n--- c) Rutas Más Utilizadas ---");
        procesador.rutasMasUtilizadas(loteDatos)
                .forEach(e -> System.out.println(e.getKey() + ": " + e.getValue()));

        System.out.println("\n--- d) Patrones de Viaje del Usuario U001 ---");
        System.out.println(procesador.obtenerPatronesDeViaje(loteDatos).get("U001"));

        System.out.println("\n--- e) Tiempo Promedio entre Estaciones (Minutos) ---");
        System.out.println(procesador.tiempoPromedioEntreEstaciones(loteDatos));

        System.out.println("\n--- f) Rutas en Sobrecarga (Umbral >= 2) ---");
        System.out.println(procesador.detectarSobrecarga(loteDatos, 2));

        // Lote grande: demuestra escalabilidad y paralelización
        List<RegistroTransito> loteGrande = generarLote(1_000_000);
        System.out.println("\n=== LOTE GRANDE (" + loteGrande.size() + " registros) ===");

        long t1 = System.nanoTime();
        var secuencial = procesador.calcularAfluencia(loteGrande);
        long t2 = System.nanoTime();
        var paralelo = procesador.calcularAfluenciaParalela(loteGrande);
        long t3 = System.nanoTime();

        System.out.println("Afluencia (secuencial): " + secuencial);
        System.out.println("Tiempo secuencial: " + (t2 - t1) / 1_000_000 + " ms");
        System.out.println("Tiempo paralelo:   " + (t3 - t2) / 1_000_000 + " ms");
        System.out.println("¿Resultados idénticos? " + secuencial.equals(paralelo));
        System.out.println("Rutas críticas (umbral 250000): "
                + procesador.detectarSobrecarga(loteGrande, 250_000));
        System.out.println("Hora pico principal: "
                + procesador.identificarHorasPico(loteGrande).get(0));
    }
}