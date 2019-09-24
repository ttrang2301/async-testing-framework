package ttrang2301.asynctesting.persistence;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class TestcaseResultMongoRepository implements TestcaseResultRepository {

    private MongoCredential credential = MongoCredential.createCredential("admin", "admin", "admin".toCharArray());
    private MongoClient mongoClient = MongoClients.create(
            MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString("mongodb://localhost:27017"))
                    .credential(credential).build()
    );
    private MongoDatabase database = mongoClient.getDatabase("async-testing-sample");
    private MongoCollection<Document> collection = database.getCollection("TestResult");

    @Override
    public void insertTestcaseResults(List<TestcaseResult> initialTestcaseResults) {
        initialTestcaseResults.forEach(this::insertTestcaseResult);
    }

    @Override
    public void insertTestcaseResult(TestcaseResult testcaseResult) {
        List<Document> expectations = testcaseResult.getExpectations().stream()
                .map(expectation ->
                        new Document("key", expectation.getKey())
                                .append("status", expectation.getStatus().getValue()))
                .collect(Collectors.toList());
        Document document = new Document("_id", UUID.randomUUID().toString())
                .append("campaignId", testcaseResult.getCampaignId())
                .append("testcaseId", testcaseResult.getTestcaseId())
                .append("expectations", expectations)
                .append("status", testcaseResult.getStatus().getValue());
        collection.insertOne(document);
    }

    @Override
    public void updateStatus(String campaignId, String testcaseId, TestcaseResult.Status status) {
        UpdateResult updateResult = collection.updateOne(
                Filters.and(Filters.eq("campaignId", campaignId), Filters.eq("testcaseId", testcaseId)),
                new Document("$set", new Document("status", status.getValue()))
        );
        if (updateResult.getModifiedCount() != 1) {
            log.error("Trying to update status of testcase {}.{} to {}. Expect modified count is 1 but encounter {}",
                    campaignId, testcaseId, status, updateResult.getModifiedCount());
        }
    }

    @Override
    public void updateExpectationStatus(String campaignId, String testcaseId,
                                        String expectationKey, TestcaseResult.Expectation.Status status) {
        UpdateResult updateResult = collection.updateOne(
                Filters.and(Filters.eq("campaignId", campaignId), Filters.eq("testcaseId", testcaseId)),
                new Document("$set", new Document("expectations.$[element].status", status.getValue())),
                new UpdateOptions().arrayFilters(Arrays.asList(Filters.in("element.key", expectationKey)))
        );
        if (updateResult.getModifiedCount() != 1) {
            log.error("Trying to update status of expectation {}.{}.{} to {}. Expect modified count is 1 but encounter {}",
                    campaignId, testcaseId, expectationKey, status, updateResult.getModifiedCount());
        }
    }
}
