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
import ttrang2301.sample.studentservice.model.Student;
import ttrang2301.sample.studentservice.repository.CreateStudentRequestRepository;
import ttrang2301.sample.studentservice.repository.StudentRepository;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/students")
public class StudentResource {

    public static final String STUDENT_CREATED_EVENT_NAME = "VirtualTopic.StudentCreated";

    @Autowired
    private StudentRepository repository;

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudent(@PathVariable String id) {
        Optional<ttrang2301.sample.studentservice.model.Student> student = repository.findById(id);
        return student.isPresent()
                ? ResponseEntity.status(HttpStatus.OK).body(new Student(student.get().getId(), student.get().getName()))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    public static final class CreateStudentRunnable implements Runnable {

        private CreateStudentRequestRepository requestRepository;
        private StudentRepository studentRepository;
        private JmsTemplate jmsTemplate;

        private CreateStudentRequestResource.CreateStudentRequest request;

        public CreateStudentRunnable(CreateStudentRequestRepository requestRepository,
                                      StudentRepository studentRepository,
                                      JmsTemplate jmsTemplate,
                                      CreateStudentRequestResource.CreateStudentRequest request) {
            this.requestRepository = requestRepository;
            this.studentRepository = studentRepository;
            this.jmsTemplate = jmsTemplate;
            this.request = request;
        }

        @Override
        public void run() {
            String createdStudentId = UUID.randomUUID().toString();
            System.out.println("START processing request " + request.getId() + " - Creating student " + createdStudentId);
            ttrang2301.sample.studentservice.model.CreateStudentRequest persistedRequest = requestRepository.findById(request.getId()).get();
            for (int i=0; i<9; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("Encounter issue ");
                    e.printStackTrace();
                }
                int progress = (i + 1) * 10;
                persistedRequest.setStatus(progress);
                persistedRequest = requestRepository.save(persistedRequest);
                System.out.println("PROCESSING request " + request.getId() + " - Creating student " + createdStudentId + ": " + progress + "%");
            }
            ttrang2301.sample.studentservice.model.Student student = new ttrang2301.sample.studentservice.model.Student(createdStudentId, request.getForm().getStudentName());
            studentRepository.save(student);
            persistedRequest.setCreatedStudentId(createdStudentId);
            persistedRequest.setStatus(100);
            requestRepository.save(persistedRequest);
            System.out.println("COMPLETE request " + request.getId() + " - Creating student " + createdStudentId);
            jmsTemplate.convertAndSend(new ActiveMQTopic(STUDENT_CREATED_EVENT_NAME), new StudentResource.Student(student.getId(), student.getName()));
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class Student {

        private String id;
        private String name;
    }

}
