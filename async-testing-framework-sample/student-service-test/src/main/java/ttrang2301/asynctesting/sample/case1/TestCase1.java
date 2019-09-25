package ttrang2301.asynctesting.sample.case1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import ttrang2301.asynctesting.exception.IgnoredEventException;
import ttrang2301.asynctesting.exception.UnexpectedException;
import ttrang2301.asynctesting.annotation.AsyncTest;
import ttrang2301.asynctesting.annotation.Expectation;
import ttrang2301.asynctesting.annotation.Precondition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@AsyncTest(name = "TC-ABC1234")
public class TestCase1 {

    private static String REQUEST_ID;
    private static String STUDENT_ID;
    private static String STUDENT_NAME = "Anna";

    @Precondition
    public void sumitCreatStudentForm() {
        Response response;
        try {
            response = sendPost(STUDENT_NAME);
        } catch (IOException e) {
            throw new UnexpectedException(e);
        }
        if (response.getHttpCode() != 202) {
            throw new UnexpectedException();
        }
        REQUEST_ID = response.getRequestId();
    }

    @Expectation(key = "form.accepted", eventName = "CreateStudentRequestCreated")
    public void assertCreateStudentRequestCreatedEvent(CreateStudentRequestCreatedEvent event) {
        if (event.getId() != REQUEST_ID) {
            throw new IgnoredEventException();
        }
        STUDENT_ID = event.getCreateStudentId();
    }

    @Expectation(key = "student.created", eventName = "StudentCreated")
    public void assertStudentCreatedEvent(StudentCreatedEvent event) {
        if (event.getId() != STUDENT_ID) {
            throw new IgnoredEventException();
        }
        if (!Objects.equal(event.getName(), STUDENT_NAME)) {
            throw new UnexpectedException("Expect name of created student: '" + STUDENT_NAME + "', actual: '" + event.getName());
        }
    }

    private Response sendPost(String name) throws IOException {
        String url = "http://localhost:8080/create-student-requests";
        HttpPost post = new HttpPost(url);
        post.setHeader("Host", "accounts.google.com");
        post.setHeader("Accept-Language", "en-US,en;q=0.5");
        post.setHeader("Connection", "keep-alive");
        post.setHeader("Content-Type", "application/json");

        ObjectMapper objectMapper = new ObjectMapper();
        byte[] bytes = objectMapper.writeValueAsString(new CreateStudentForm(name)).getBytes();
        post.setEntity(new ByteArrayEntity(bytes));

        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = client.execute(post);

        int responseCode = response.getStatusLine().getStatusCode();

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        return new Response(responseCode, objectMapper.readValue(result.toString(), CreateStudentRequestId.class).getRequestId());
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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class CreateStudentRequestId {

        private String requestId;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class Response {

        private int httpCode;
        private String requestId;

    }

}
