package com.eliteshop.colombia.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

class ProfileConfigurationTest {

  private final Yaml yaml = new Yaml();

  @Test
  void shouldHaveOptionalConfigServerWhenLocalProfile() throws IOException {
    var config = loadProfile("application-local.yml");
    var springMap = (java.util.Map<?, ?>) config.get("spring");
    var configMap = (java.util.Map<?, ?>) springMap.get("config");

    assertThat(configMap.get("import").toString()).contains("optional:configserver:");
  }

  @Test
  void shouldHaveMandatoryConfigServerWhenDevProfile() throws IOException {
    var config = loadProfile("application-dev.yml");
    var springMap = (java.util.Map<?, ?>) config.get("spring");
    var configMap = (java.util.Map<?, ?>) springMap.get("config");

    String configImport = configMap.get("import").toString();
    assertThat(configImport).startsWith("configserver:");
    assertThat(configImport).doesNotContain("optional:");
  }

  @Test
  void shouldHaveMandatoryConfigServerWhenProdProfile() throws IOException {
    var config = loadProfile("application-prod.yml");
    var springMap = (java.util.Map<?, ?>) config.get("spring");
    var configMap = (java.util.Map<?, ?>) springMap.get("config");

    String configImport = configMap.get("import").toString();
    assertThat(configImport).startsWith("configserver:");
    assertThat(configImport).doesNotContain("optional:");
  }

  @Test
  void shouldDisableConfigServerWhenTestProfile() throws IOException {
    var config = loadProfile("application-test.yml", "test");
    var springMap = (java.util.Map<?, ?>) config.get("spring");
    var cloudMap = (java.util.Map<?, ?>) springMap.get("cloud");
    var configMap = (java.util.Map<?, ?>) cloudMap.get("config");

    assertThat(configMap.get("enabled")).isEqualTo(false);
  }

  @Test
  void shouldUseH2DatabaseWhenTestProfile() throws IOException {
    var config = loadProfile("application-test.yml", "test");
    var springMap = (java.util.Map<?, ?>) config.get("spring");
    var datasourceMap = (java.util.Map<?, ?>) springMap.get("datasource");

    assertThat(datasourceMap.get("url").toString()).contains("h2:mem:");
    assertThat(datasourceMap.get("driver-class-name").toString()).contains("h2");
  }

  @Test
  void shouldDisableLiquibaseWhenTestProfile() throws IOException {
    var config = loadProfile("application-test.yml", "test");
    var springMap = (java.util.Map<?, ?>) config.get("spring");
    var liquibaseMap = (java.util.Map<?, ?>) springMap.get("liquibase");

    assertThat(liquibaseMap.get("enabled")).isEqualTo(false);
  }

  @Test
  void shouldHaveJwtSecretWhenTestProfile() throws IOException {
    var config = loadProfile("application-test.yml", "test");
    var jwtMap = (java.util.Map<?, ?>) config.get("jwt");

    assertThat(jwtMap.get("secret")).isNotNull();
    assertThat(jwtMap.get("secret").toString()).isNotBlank();
  }

  private java.util.Map<?, ?> loadProfile(String filename) throws IOException {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
      return yaml.load(is);
    }
  }

  private java.util.Map<?, ?> loadProfile(String filename, String profile) throws IOException {
    return loadProfile(filename);
  }
}
