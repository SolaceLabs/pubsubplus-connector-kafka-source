package com.solace.messaging.kafka.it;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.wait.strategy.Wait;

@Testcontainers
public interface MessagingServiceFullLocalSetupConfluent  extends TestConstants {

  @Container
  public static final DockerComposeContainer COMPOSE_CONTAINER_PUBSUBPLUS =
      new DockerComposeContainer(
          new File(FULL_DOCKER_COMPOSE_FILE_PATH + "docker-compose-solace.yml"))
          .withEnv("PUBSUB_NETWORK_NAME", PUBSUB_NETWORK_NAME)
          .withEnv("PUBSUB_HOSTNAME", PUBSUB_HOSTNAME)
          .withEnv("PUBSUB_TAG", PUBSUB_TAG)
          .withServices(SERVICES)
          .withLocalCompose(true)
          .withPull(false)
          .waitingFor("solbroker_1",
                          Wait.forLogMessage(".*System startup complete.*", 1) );

  @Container
  public static final DockerComposeContainer COMPOSE_CONTAINER_KAFKA =
        new DockerComposeContainer(
            new File(FULL_DOCKER_COMPOSE_FILE_PATH + "docker-compose-kafka-confluent.yml"))
            .withEnv("KAFKA_TOPIC", KAFKA_SOURCE_TOPIC)
            .withEnv("KAFKA_HOST", COMPOSE_CONTAINER_PUBSUBPLUS.getServiceHost("solbroker_1", 8080))
            .withLocalCompose(true)
            .waitingFor("schema-registry_1",
                Wait.forHttp("/subjects").forStatusCode(200));

  @BeforeAll
  static void checkContainer() {
    String host = COMPOSE_CONTAINER_PUBSUBPLUS.getServiceHost("solbroker_1", 8080);
    assertNotNull(host);
  }
}

