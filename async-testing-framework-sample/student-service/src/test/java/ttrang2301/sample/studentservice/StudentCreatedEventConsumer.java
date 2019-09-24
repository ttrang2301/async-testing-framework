package ttrang2301.sample.studentservice;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import ttrang2301.sample.studentservice.resource.StudentResource;

@Slf4j
@Component
public class StudentCreatedEventConsumer {

    @JmsListener(destination = "Consumer.Stalker." + StudentResource.STUDENT_CREATED_EVENT_NAME)
    public void receiveTopicMessage(@Payload StudentResource.Student student) {
        log.info("received <" + student + ">");
    }

}
