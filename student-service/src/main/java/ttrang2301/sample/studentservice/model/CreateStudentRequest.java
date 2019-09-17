package ttrang2301.sample.studentservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CreateStudentRequest {

    private String id;
    private String studentName;
    private int status;
    private String createStudentId;

    public CreateStudentRequest(String studentName) {
        this.id = UUID.randomUUID().toString();
        this.studentName = studentName;
        this.status = 0;
        this.createStudentId = UUID.randomUUID().toString();
    }
}
