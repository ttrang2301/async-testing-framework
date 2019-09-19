package ttrang2301.sample.studentservice.resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ttrang2301.sample.studentservice.model.Student;

@RestController("/students")
public class StudentResource {

    @GetMapping
    public Student getStudentByName(@RequestParam String name) {
        // TODO
        return null;
    }

}
