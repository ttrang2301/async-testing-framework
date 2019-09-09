package ttrang2301.asynctesting.test.case1;

import ttrang2301.asynctesting.annotation.AsyncTest;
import ttrang2301.asynctesting.annotation.Expectation;
import ttrang2301.asynctesting.annotation.Precondition;

@AsyncTest(name = "TC-ABS1234")
public class TestCase1 {

    // Executed -> Waiting -> Done
    // Executed | Waiting | Done | Timeout

    @Precondition
    public void createCreateStudentRequest() {
        // TODO
        // sendCreateStudentRequest(new CreateStudentRequest("A", "Male"));
    }

    @Expectation
    public void assertStudentCreatedEvent() {
//  public void assertStudentCreatedEvent(StudentCreatedEvent eventName) {
        // assertThat(eventName.student.name == "A");
        // assertThat(eventName.student.gender == "Male");
        // studentRepository.get();
    }

}
