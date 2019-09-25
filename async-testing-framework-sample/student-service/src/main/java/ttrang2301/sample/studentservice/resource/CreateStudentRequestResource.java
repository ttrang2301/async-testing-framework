package ttrang2301.sample.studentservice.resource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;
import ttrang2301.sample.studentservice.repository.CreateStudentRequestRepository;
import ttrang2301.sample.studentservice.repository.StudentRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/create-student-requests")
public class CreateStudentRequestResource {

    public static final String CREATE_STUDENT_REQUEST_CREATED_EVENT_NAME = "VirtualTopic.CreateStudentRequestCreated";

    @Autowired
    private CreateStudentRequestRepository requestRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private JmsTemplate jmsTemplate;

    @PostMapping
    public ResponseEntity<CreateStudentRequestId> createCreateStudentRequest(@RequestBody CreateStudentForm form) {
        String requestId = UUID.randomUUID().toString();
        CreateStudentRequest request = new CreateStudentRequest(requestId, form, 0, null);
        requestRepository.save(new ttrang2301.sample.studentservice.model.CreateStudentRequest(requestId, form.getStudentName(), 0, null));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new StudentResource.CreateStudentRunnable(requestRepository, studentRepository, jmsTemplate, request));
        jmsTemplate.convertAndSend(new ActiveMQTopic(CREATE_STUDENT_REQUEST_CREATED_EVENT_NAME), request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new CreateStudentRequestId(requestId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreateStudentRequest> getCreateStudentRequest(
            @PathVariable String id) {
        Optional<ttrang2301.sample.studentservice.model.CreateStudentRequest> request = requestRepository.findById(id);
        if (!request.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(new CreateStudentRequest(
                request.get().getId(),
                new CreateStudentForm(request.get().getForm().getStudentName()),
                request.get().getStatus(),
                request.get().getCreatedStudentId()));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class CreateStudentRequest {

        private String id;
        private CreateStudentRequestResource.CreateStudentForm form;
        private int status;
        private String createStudentId;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class CreateStudentForm {

        private String studentName;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class CreateStudentRequestId {

        private String requestId;

    }

}
