package ttrang2301.asynctesting.persistence;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.*;
import java.util.stream.Collectors;

public class TestcaseResultMongoRepository implements TestcaseResultRepository {

    private MongoCredential credential = MongoCredential.createCredential("admin", "admin", "admin".toCharArray());
    private MongoClient mongoClient = MongoClients.create(
            MongoClientSettings.builder().credential(credential).build()
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
        collection.updateOne(
                Filters.and(Filters.eq("campaignId", campaignId), Filters.eq("testcaseId", testcaseId)),
                new Document("$set", new Document("status", status.getValue()))
        );
    }

    @Override
    public void updateExpectationStatus(String campaignId, String testcaseId,
                                        String expectationKey, TestcaseResult.Expectation.Status status) {
        // TODO updateExpectationStatus(String campaignId, String testcaseId, String expectationKey, TestcaseResult.Expectation.Status status)
        collection.updateOne(
                Filters.and(Filters.eq("campaignId", campaignId), Filters.eq("testcaseId", testcaseId)),
                new Document("$set", new Document("expectations", status.getValue()))
        );
    }
}
