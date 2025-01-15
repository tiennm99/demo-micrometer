package com.miti99;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Timer;
import io.micrometer.elastic.ElasticConfig;
import io.micrometer.elastic.ElasticMeterRegistry;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MicrometerDemo {

  private static final Random random = new Random();

  public static void main(String[] args) {
    var config =
        new ElasticConfig() {
          @Override
          public boolean autoCreateIndex() {
            return true;
          }

          @Override
          public String get(String key) {
            return switch (key) {
              case "elastic.index" -> "metrics";
              case "elastic.index-date-format" -> "yyyy-MM-dd";
              default -> null;
            };
          }

          @Override
          public String host() {
            return "http://localhost:9200";
          }

          @Override
          public Duration step() {
            return Duration.ofSeconds(10);
          }
        };

    var registry = new ElasticMeterRegistry(config, Clock.SYSTEM);

    var requestCounter =
        Counter.builder("demo.requests")
            .description("Total number of requests")
            .tag("type", "demo")
            .register(registry);

    var processTimer =
        Timer.builder("demo.process.time")
            .description("Time spent processing")
            .tag("type", "demo")
            .register(registry);

    var memoryGauge =
        Gauge.builder(
                "demo.memory",
                Runtime.getRuntime(),
                runtime -> runtime.totalMemory() - runtime.freeMemory())
            .description("JVM memory usage")
            .tag("type", "demo")
            .register(registry);

    var userCount = new AtomicInteger(0);
    var userGauge =
        Gauge.builder("demo.users", userCount, AtomicInteger::get)
            .description("Number of active users")
            .tag("type", "demo")
            .register(registry);

    registry
        .config()
        .onMeterRegistrationFailed(
            (a, b) -> {
              log.error("Failed to publish metrics {} {}", a, b);
            });

    registry.start();

    log.info("Starting metrics demo simulation...");

    while (true) {
      try {
        requestCounter.increment();

        processTimer.record(
            () -> {
              try {
                TimeUnit.MILLISECONDS.sleep(random.nextInt(100));
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            });

        userCount.set(random.nextInt(100));

        log.info(
            "Metrics update - Requests: {}, Users: {}, Memory: {} MB",
            requestCounter.count(),
            userCount.get(),
            memoryGauge.value() / (1024 * 1024));

        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }
}
