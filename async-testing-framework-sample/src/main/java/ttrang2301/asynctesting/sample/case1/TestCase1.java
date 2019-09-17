package ttrang2301.asynctesting.sample.case1;

import ttrang2301.asynctesting.annotation.AsyncTest;
import ttrang2301.asynctesting.annotation.Expectation;
import ttrang2301.asynctesting.annotation.Precondition;

@AsyncTest(name = "TC-ABS1234")
public class TestCase1 {

    // Executed -> Waiting -> Done
    // Executed | Waiting | Done | Timeout

    @Precondition
    public void createCreateStudentRequest3() {
        // TODO
        // sendCreateStudentRequest(new CreateStudentRequest("A", "Male"));
        System.out.println("Public Preconsitions initialized");
    }

    @Expectation(key = "", eventName = "")
    public void assertStudentCreatedEvent(Object e) {
//  public void assertStudentCreatedEvent(StudentCreatedEvent eventName) {
        // assertThat(eventName.student.name == "A");
        // assertThat(eventName.student.gender == "Male");
        // studentRepository.get();
    }

}
