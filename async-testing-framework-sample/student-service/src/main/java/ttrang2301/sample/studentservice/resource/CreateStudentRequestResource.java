package ttrang2301.sample.studentservice.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ttrang2301.sample.studentservice.model.CreateStudentForm;
import ttrang2301.sample.studentservice.model.CreateStudentRequest;
import ttrang2301.sample.studentservice.model.CreateStudentRequestStatus;
import ttrang2301.sample.studentservice.model.Student;
import ttrang2301.sample.studentservice.repository.CreateStudentRequestRepository;
import ttrang2301.sample.studentservice.repository.StudentRepository;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController("/create-student-request")
public class CreateStudentRequestResource {

    @Autowired
    private CreateStudentRequestRepository requestRepository;

    @Autowired
    private StudentRepository studentRepository;

    @PostMapping
    public ResponseEntity<String> submitCreateStudentRequest(@RequestBody CreateStudentForm form) {
        String requestId = UUID.randomUUID().toString();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new CreateStudentRunnable(requestRepository, studentRepository, form));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(requestId);
    }

    @GetMapping("/{id}")
    public CreateStudentRequest getCreateStudentRequest(@PathVariable String id) {
        return requestRepository.getOne(id);
    }

    private static final class CreateStudentRunnable implements Runnable {

        private CreateStudentRequestRepository requestRepository;
        private StudentRepository studentRepository;

        private CreateStudentForm form;

        private CreateStudentRunnable(CreateStudentRequestRepository requestRepository,
                                      StudentRepository studentRepository,
                                      CreateStudentForm form) {
            this.requestRepository = requestRepository;
            this.studentRepository = studentRepository;
            this.form = form;
        }

        @Override
        public void run() {
            CreateStudentRequest request = new CreateStudentRequest(this.form.getStudentName());
            request = requestRepository.save(request);
            for (int i=0; i<10; i++) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                request.setStatus((i + 1) * 10);
                requestRepository.save(request);
            }
            studentRepository.save(new Student(request.getCreateStudentId(), request.getStudentName()));
        }
    }

}
