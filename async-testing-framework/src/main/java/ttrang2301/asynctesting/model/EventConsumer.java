package ttrang2301.asynctesting.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventConsumer {

    private String eventName;
    private Expectation expectation;
    private String testcaseId;

    public static final ttrang2301.asynctesting.persistence.EventConsumer toPersistedModel(EventConsumer model) {
        // TODO
        return null;
    }

}
