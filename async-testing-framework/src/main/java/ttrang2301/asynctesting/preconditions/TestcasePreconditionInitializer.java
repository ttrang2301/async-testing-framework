package ttrang2301.asynctesting.preconditions;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TestcasePreconditionInitializer {

    private List<Precondition> preconditions;

}
