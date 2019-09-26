package ttrang2301.asynctesting.expectations;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ttrang2301.asynctesting.persistence.TestcaseResult;
import ttrang2301.asynctesting.persistence.TestcaseResultRepository;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@AllArgsConstructor
public class PeriodicallyExpectationScanningService implements Runnable {

    private String campaignId;

    private TestcaseResultRepository repository;

    @Override
    public void run() {
        while (true) {
            List<TestcaseResult> results = repository.findAllByCampaignIdAndStatusReady(campaignId);
            boolean testcaseWaiting = false;
            for (TestcaseResult result : results) {
                boolean completionPointWaiting = false;
                for (TestcaseResult.CompletionPoint completionPoint : result.getCompletionPoints()) {
                    if (completionPoint.getStatus() == TestcaseResult.CompletionPoint.Status.UNKNOWN) {
                        completionPointWaiting = true;
                        break;
                    }
                }
                if (!completionPointWaiting) {
                    repository.updateStatus(campaignId, result.getTestcaseId(), TestcaseResult.Status.SUCCESSFUL);
                } else {
                    testcaseWaiting = true;
                }
            }
            if (!testcaseWaiting) {
                stopTestingApplicationAsSuccessful();
            }
            try {
                TimeUnit.SECONDS.sleep(30);
            } catch (InterruptedException e) {
                log.error("Unexpected exception: trying to wait for next scan but fail", e);
            }
        }
    }

    private void stopTestingApplicationAsSuccessful() {
        System.exit(0);
    }

}
