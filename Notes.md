# Kafka

## Concepts

### Partitions

Kafka organizes messages into topics, and each topic is divided into multiple partitions to enable parallel processing.

Message ordering is guaranteed within a partition, but not across partitions

Each Kafka topic can have its own independent partition count, and partitions are not shared across topics.

### Consumer Group

Consumer Group allows parallel processing of messages within a topic.

It is not possible to have more active consumers in a consumer group than the number of partitions for a topic.

### Consumer Offsets

Kafka stores consumer group offsets in a special internal topic named `__consumer_offsets`.

Use Manual Offset Commit to ensure avoid handling duplicate messages at the expense of speed (sync commits add latency).

Rebalancing and Ordering: If a consumer crashes or leaves the group, Kafka triggers rebalancing, and another consumer is assigned to the partition that the failed consumer was consuming from. During this rebalancing, Kafka ensures that the consumer takes over at the correct offset, maintaining the order.

Other than subscribing using consumer groups, you can also 'assign' to partitions you want to read by using empty group ID.


```
# How a consumer group behaves when no initial committed consumer offset is available.
auto.offset.reset=latest

# Time after which inactive group offsets are deleted (default: 7 days)
offsets.retention.minutes=10080  

# Periodically commit offset of the last message handed to the application (default: true)
enable.auto.commit=false

# The frequency in milliseconds that the consumer offsets are committed (written) to offset storage
auto.commit.interval.ms=5000

# Client will automatically store the offset+1 of the message just prior to passing the message to the application. The offset is stored in the memory and will be used by the next call to commit() (without explicit offsets specified) or the next auto commit. If false and enable.auto.commit=true, the application will manually have to call rd_kafka_offset_store() to store the offset to auto commit. (optional).
enable.auto.offset.store=false
```

## Producer

### Idempotent Producer

> Enabled by default from Kafka 3.0 onwards

When `enable.idempotence=true` is set, `acks=all` is also set implicitly to ensure messages sent requires acknowledgement by all in-sync replicas. Overriding `acks` to other values will result in `ConfigException`.

> This does not ensure idempotence at application level if there are network issues between client and application (e.g. invoking application to send the same message twice).

### Producer Partitioning Strategy

From Kafka 2.4 onwards, clients will use Sticky Partitioning to improve batching.

### Producer Retries

Allowing retries without setting `max.in.flight.requests.per.connection=1` might not guarantee message ordering because if two batches are sent to a single partition, and the first fails and is retried but the second succeeds, then the records in the second batch may appear first.

However, if `enable.idempotence=true` is set, the broker will buffer out-of-order messages using sequence number and only commit them in sequence.

## Kafka CLI

```sh
# Create topic
# NOTE: You cannot specify a replication factor greater than the number of brokers you have
./kafka-topics.sh --bootstrap-server localhost:9092 --topic firsttopic --create --partitions 3 --replication-factor 1

# List topics
./kafka-topics.sh --bootstrap-server localhost:9092 --list

# Describe topics
./kafka-topics.sh --bootstrap-server 127.0.0.1:9092 --describe --topic firsttopic
```

## Reference

[Kafka Advanced Concepts](https://learn.conduktor.io/kafka/kafka-advanced-concepts/)