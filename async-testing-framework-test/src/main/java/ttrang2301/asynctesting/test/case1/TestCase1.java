package ttrang2301.asynctesting.test.case1;

import ttrang2301.asynctesting.AsyncTest;
import ttrang2301.asynctesting.Expectation;
import ttrang2301.asynctesting.Precondition;

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
//  public void assertStudentCreatedEvent(StudentCreatedEvent event) {
        // assertThat(event.student.name == "A");
        // assertThat(event.student.gender == "Male");
        // studentRepository.get();
    }

}
