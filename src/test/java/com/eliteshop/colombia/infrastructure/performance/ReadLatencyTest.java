package com.eliteshop.colombia.infrastructure.performance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * CP-36 — Tiempo de respuesta de las operaciones de lectura.
 *
 * <p>Ejecuta cada endpoint de lectura un número suficiente de veces y verifica que el percentil 95
 * se mantenga por debajo del umbral de dos segundos definido en los criterios de aceptación.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReadLatencyTest {

  private static final int ITERATIONS = 30;
  private static final long THRESHOLD_MS = 2_000L;

  @Autowired private MockMvc mockMvc;

  private long percentile95(List<Long> samples) {
    List<Long> sorted = new ArrayList<>(samples);
    Collections.sort(sorted);
    int index = (int) Math.ceil(0.95 * sorted.size()) - 1;
    return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
  }

  private void measure(String path, String label) throws Exception {
    List<Long> samples = new ArrayList<>();
    for (int i = 0; i < ITERATIONS; i++) {
      long start = System.nanoTime();
      mockMvc.perform(get(path)).andReturn();
      samples.add((System.nanoTime() - start) / 1_000_000);
    }
    long p95 = percentile95(samples);
    double average = samples.stream().mapToLong(Long::longValue).average().orElse(0);
    System.out.printf(
        "[CP-36] %-28s n=%d promedio=%.1f ms p95=%d ms umbral=%d ms%n",
        label, samples.size(), average, p95, THRESHOLD_MS);
    assertThat(p95)
        .as("percentil 95 de %s debe ser menor a %d ms", label, THRESHOLD_MS)
        .isLessThan(THRESHOLD_MS);
  }

  @Test
  void catalogReadShouldStayUnderThreshold() throws Exception {
    measure("/api/v1/products?page=0&size=10", "Catálogo de productos");
  }

  @Test
  void reviewsReadShouldStayUnderThreshold() throws Exception {
    measure("/api/v1/reviews?page=0&size=10", "Listado de reseñas");
  }

  @Test
  void locationsReadShouldStayUnderThreshold() throws Exception {
    measure("/api/v1/locations/departments", "Catálogo de ubicaciones");
  }
}
