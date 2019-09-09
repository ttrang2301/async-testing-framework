package ttrang2301.asynctesting.persistence;

import java.util.List;

public interface EventConsumerRepository {

    void insertEventConsumers(List<EventConsumer> eventConsumers);

}
