package ttrang2301.asynctesting.sample.case1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ttrang2301.asynctesting.annotation.AsyncTest;
import ttrang2301.asynctesting.annotation.Expectation;
import ttrang2301.asynctesting.annotation.Precondition;

@AsyncTest(name = "TC-ABC1234")
public class TestCase1 {

    @Precondition
    public void sumitCreatStudentForm() {
        // TODO sumitCreatStudentForm
        System.out.println("Public Preconsitions initialized");
    }

    @Expectation(key = "form.accepted", eventName = "CreateStudentRequestCreated")
    public void assertCreateStudentRequestCreatedEvent(CreateStudentRequestCreatedEvent event) {
        // TODO assertCreateStudentRequestCreatedEvent
    }

    @Expectation(key = "student.created", eventName = "StudentCreated")
    public void assertStudentCreatedEvent(StudentCreatedEvent event) {
        // TODO assertStudentCreatedEvent
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class CreateStudentRequestCreatedEvent {

        private String id;
        private CreateStudentForm form;
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
    public static final class StudentCreatedEvent {

        private String id;
        private String name;
    }

}
