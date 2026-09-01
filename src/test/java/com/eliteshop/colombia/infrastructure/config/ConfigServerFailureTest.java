package com.eliteshop.colombia.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

class ConfigServerFailureTest {

  private final Yaml yaml = new Yaml();

  @Test
  void shouldUseOptionalConfigServerWhenLocalProfile() throws IOException {
    Map<?, ?> config = loadConfig("application-local.yml");
    Map<?, ?> spring = (Map<?, ?>) config.get("spring");
    Map<?, ?> springConfig = (Map<?, ?>) spring.get("config");
    String configImport = springConfig.get("import").toString();

    assertThat(configImport).contains("optional:configserver:");
  }

  @Test
  void shouldUseMandatoryConfigServerWhenDevProfile() throws IOException {
    Map<?, ?> config = loadConfig("application-dev.yml");
    Map<?, ?> spring = (Map<?, ?>) config.get("spring");
    Map<?, ?> springConfig = (Map<?, ?>) spring.get("config");
    String configImport = springConfig.get("import").toString();

    assertThat(configImport).startsWith("configserver:");
    assertThat(configImport).doesNotContain("optional:");
  }

  @Test
  void shouldUseMandatoryConfigServerWhenProdProfile() throws IOException {
    Map<?, ?> config = loadConfig("application-prod.yml");
    Map<?, ?> spring = (Map<?, ?>) config.get("spring");
    Map<?, ?> springConfig = (Map<?, ?>) spring.get("config");
    String configImport = springConfig.get("import").toString();

    assertThat(configImport).startsWith("configserver:");
    assertThat(configImport).doesNotContain("optional:");
  }

  @Test
  void shouldDisableConfigServerWhenTestProfile() throws IOException {
    Map<?, ?> config = loadConfig("application-test.yml");
    Map<?, ?> spring = (Map<?, ?>) config.get("spring");
    Map<?, ?> cloud = (Map<?, ?>) spring.get("cloud");
    Map<?, ?> cloudConfig = (Map<?, ?>) cloud.get("config");

    assertThat(cloudConfig.get("enabled")).isEqualTo(false);
  }

  @Test
  void shouldSurviveWhenConfigServerIsDown() throws IOException {
    Map<?, ?> config = loadConfig("application-local.yml");
    Map<?, ?> spring = (Map<?, ?>) config.get("spring");
    Map<?, ?> springConfig = (Map<?, ?>) spring.get("config");
    String configImport = springConfig.get("import").toString();

    assertThat(configImport).startsWith("optional:configserver:");
  }

  @Test
  void shouldFailWhenConfigServerIsDown() throws IOException {
    Map<?, ?> config = loadConfig("application-dev.yml");
    Map<?, ?> spring = (Map<?, ?>) config.get("spring");
    Map<?, ?> springConfig = (Map<?, ?>) spring.get("config");
    String configImport = springConfig.get("import").toString();

    assertThat(configImport).startsWith("configserver:");
    assertThat(configImport).doesNotContain("optional:");
  }

  private Map<?, ?> loadConfig(String filename) throws IOException {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
      return yaml.load(is);
    }
  }
}
