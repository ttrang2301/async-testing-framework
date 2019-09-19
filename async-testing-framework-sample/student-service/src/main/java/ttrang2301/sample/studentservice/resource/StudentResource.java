package ttrang2301.sample.studentservice.resource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ttrang2301.sample.studentservice.model.Student;
import ttrang2301.sample.studentservice.repository.StudentRepository;

import java.util.Optional;

@RestController
@RequestMapping("/students")
public class StudentResource {

    @Autowired
    private StudentRepository repository;

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudent(@PathVariable String id) {
        Optional<ttrang2301.sample.studentservice.model.Student> student = repository.findById(id);
        return student.isPresent()
                ? ResponseEntity.status(HttpStatus.OK).body(new Student(student.get().getId(), student.get().getName()))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static final class Student {

        private String id;
        private String name;
    }

}
