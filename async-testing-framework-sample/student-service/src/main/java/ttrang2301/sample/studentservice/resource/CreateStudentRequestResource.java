package ttrang2301.sample.studentservice.resource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ttrang2301.sample.studentservice.model.CreateStudentRequest;
import ttrang2301.sample.studentservice.model.Student;
import ttrang2301.sample.studentservice.repository.CreateStudentRequestRepository;
import ttrang2301.sample.studentservice.repository.StudentRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/create-student-requests")
public class CreateStudentRequestResource {

    @Autowired
    private CreateStudentRequestRepository requestRepository;

    @Autowired
    private StudentRepository studentRepository;

    @PostMapping
    public ResponseEntity<String> createCreateStudentRequest(@RequestBody CreateStudentForm form) {
        String requestId = UUID.randomUUID().toString();
        CreateStudentRequest request = new CreateStudentRequest(requestId, form, 0, null);
        requestRepository.save(new ttrang2301.sample.studentservice.model.CreateStudentRequest(requestId, form.getStudentName(), 0, null));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new CreateStudentRunnable(requestRepository, studentRepository, request));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(requestId);
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
    private static final class CreateStudentRequest {

        private String id;
        private CreateStudentRequestResource.CreateStudentForm form;
        private int status;
        private String createStudentId;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static final class CreateStudentForm {

        private String studentName;

    }

    private static final class CreateStudentRunnable implements Runnable {

        private CreateStudentRequestRepository requestRepository;
        private StudentRepository studentRepository;

        private CreateStudentRequestResource.CreateStudentRequest request;

        private CreateStudentRunnable(CreateStudentRequestRepository requestRepository,
                                      StudentRepository studentRepository,
                                      CreateStudentRequestResource.CreateStudentRequest request) {
            this.requestRepository = requestRepository;
            this.studentRepository = studentRepository;
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
            Student student = new Student(createdStudentId, request.getForm().getStudentName());
            studentRepository.save(student);
            persistedRequest.setCreatedStudentId(createdStudentId);
            persistedRequest.setStatus(100);
            requestRepository.save(persistedRequest);
            System.out.println("COMPLETE request " + request.getId() + " - Creating student " + createdStudentId);
        }
    }

}
