package com.example;

import static java.lang.String.format;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.StreamRecord;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.util.json.Jackson;

/**
 * Handles events (upserts) to the Land Observations Dynamo DB table(s), pushing
 * the records onto the configured Kafka topic for further dissemination
 *
 */
public class LandObsDDBEventProcessor implements RequestHandler<DynamodbEvent, String> {

    private static final Logger LOG = Logger.getLogger(LandObsDDBEventProcessor.class);

    private final KafkaProducer<String, String> kafkaProducer;

    private final Properties kafkaProps = new Properties();

    public LandObsDDBEventProcessor() throws IOException {
        kafkaProps.load(getClass().getResourceAsStream("/kafka.properties"));
        kafkaProducer = new KafkaProducer<>(kafkaProps);
    }

    @Override
    public String handleRequest(final DynamodbEvent ddbEvent, final Context context) {

        ddbEvent.getRecords().parallelStream().forEach(record -> {

            // The actual stream record containing the fields etc:
            StreamRecord streamRecord = record.getDynamodb();
            String key = streamRecord.getKeys().get("id").getS();

            Map<String, AttributeValue> landObsHourly = streamRecord.getNewImage().get("land_obs_hourly").getM();
            Map<String, String> result = new HashMap<>();

            landObsHourly.forEach((mapKey, attrVal) -> {
                result.put(mapKey, attrVal.getS());
            });

            String json = Jackson.toJsonString(result);

            LOG.trace("New land obs record from DynamoDB: " + json);

            ProducerRecord<String, String> landObsRecord =
                    new ProducerRecord<>(kafkaProps.getProperty("topic"), key, json);

            kafkaProducer.send(landObsRecord, (rmd, e) -> {
                if (e == null) {
                    LOG.debug(format("Msg with key: %s produced to topic: %s on partition: %d with offset: %d", key,
                            rmd.topic(), rmd.partition(), rmd.offset()));
                } else {
                    LOG.error("Unable to produce message with key: " + key + " to Kafka topic", e.getCause());
                }
            });
        });

        return "Returning from LandObsDDBEventProcessor.handleRequest";
    }
}
