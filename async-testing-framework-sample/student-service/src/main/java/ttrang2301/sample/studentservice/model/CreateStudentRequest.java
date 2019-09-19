package ttrang2301.sample.studentservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "CreateStudentRequest")
@Data
@NoArgsConstructor
public class CreateStudentRequest {

    @Id
    private String id;

    @Convert(converter = FormConverter.class)
    @Column(name = "studentName")
    private CreateStudentForm form;

    private int status;

    private String createdStudentId;

    public CreateStudentRequest(String id, String studentName, int status, String createdStudentId) {
        this.id = id;
        this.form = new CreateStudentForm(studentName);
        this.status = status;
        this.createdStudentId = createdStudentId;
    }

    @Converter
    public static final class FormConverter implements AttributeConverter<CreateStudentForm, String> {
        @Override
        public String convertToDatabaseColumn(CreateStudentForm form) {
            return (form != null && form.getStudentName() != null) ? form.getStudentName() : "";
        }

        @Override
        public CreateStudentForm convertToEntityAttribute(String value) {
            return new CreateStudentForm(value);
        }
    }
}
