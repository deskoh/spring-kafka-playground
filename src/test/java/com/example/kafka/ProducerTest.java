package com.example.kafka;

import com.example.kafka.service.Producer;
import org.apache.kafka.clients.consumer.Consumer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class ProducerTest {

    @Autowired
    Producer producer;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    @Value("${test.topic}")
    private String topic;

    private Consumer<String, String> testConsumer;

    @BeforeAll
    public void setUp() {
        // arrange
        // Create a fresh consumer for each test
        this.testConsumer = consumerFactory.createConsumer();
        testConsumer.subscribe(List.of(topic));
    }

    @AfterAll
    public void tearDown() {
        this.testConsumer.close();
    }

    @Test
    public void whenSendingWithProducer_messageCanBeReceived() throws Exception {
        // act
        String data = "Sending with our own simple KafkaProducer";
        producer.sendMessage(topic, data);

        // assert
        var receivedRecord = KafkaTestUtils.getSingleRecord(testConsumer, topic);
        assertThat(receivedRecord.value(), containsString(data));
    }

    @Test
    public void whenSendingWithProducer_messageCanBeReceived2() throws Exception {
        // act
        String data = "Sending with our own simple KafkaProducer2";
        producer.sendMessage(topic, data);

        // assert
        var receivedRecord = KafkaTestUtils.getSingleRecord(testConsumer, topic);
        assertThat(receivedRecord.value(), containsString(data));
    }
}
