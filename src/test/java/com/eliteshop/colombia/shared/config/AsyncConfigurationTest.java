package com.eliteshop.colombia.shared.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = AsyncConfiguration.class)
@ActiveProfiles("test")
class AsyncConfigurationTest {

  @Autowired private AsyncConfiguration asyncConfiguration;

  @Test
  void shouldCreateTaskExecutor() {
    Executor executor = asyncConfiguration.getAsyncExecutor();
    assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
  }

  @Test
  void shouldHaveCorrectPoolSettings() {
    ThreadPoolTaskExecutor executor =
        (ThreadPoolTaskExecutor) asyncConfiguration.getAsyncExecutor();
    assertThat(executor.getCorePoolSize()).isEqualTo(4);
    assertThat(executor.getMaxPoolSize()).isEqualTo(8);
    assertThat(executor.getQueueCapacity()).isEqualTo(50);
    assertThat(executor.getThreadNamePrefix()).isEqualTo("async-");
  }

  @Test
  void shouldHaveExceptionHandler() {
    assertThat(asyncConfiguration.getAsyncUncaughtExceptionHandler()).isNotNull();
  }
}
