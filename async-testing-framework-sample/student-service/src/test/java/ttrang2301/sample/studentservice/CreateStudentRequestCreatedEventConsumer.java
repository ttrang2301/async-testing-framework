package ttrang2301.sample.studentservice;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import ttrang2301.sample.studentservice.resource.CreateStudentRequestResource;

@Slf4j
@Component
public class CreateStudentRequestCreatedEventConsumer {

    @JmsListener(destination = "Consumer.Stalker." + CreateStudentRequestResource.CREATE_STUDENT_REQUEST_CREATED_EVENT_NAME)
    public void receiveTopicMessage(@Payload CreateStudentRequestResource.CreateStudentRequest request) {
        log.info("received <" + request + ">");
    }

}
