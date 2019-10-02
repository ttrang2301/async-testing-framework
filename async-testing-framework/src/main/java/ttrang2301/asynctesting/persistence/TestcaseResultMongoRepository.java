package ttrang2301.asynctesting.persistence;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import lombok.extern.slf4j.Slf4j;

import static org.bson.codecs.configuration.CodecRegistries.fromCodecs;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

@Slf4j
public class TestcaseResultMongoRepository implements TestcaseResultRepository {

    private MongoCollection<Document> collection;

    public TestcaseResultMongoRepository() {
        MongoCredential credential = MongoCredential.createCredential("admin", "admin", "admin".toCharArray());
        CodecRegistry pojoCodecRegistry = CodecRegistries
                .fromRegistries(
                        MongoClientSettings.getDefaultCodecRegistry(),
                        fromProviders(PojoCodecProvider.builder().automatic(true).build()),
                        fromCodecs(new TestcaseResult.TestcaseStatusCodec(), new TestcaseResult.CompletionPointStatusCodec())

                );
        MongoClient mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString("mongodb://localhost:27017"))
                        .credential(credential).build()
        );
        MongoDatabase database = mongoClient.getDatabase("async-testing-sample").withCodecRegistry(pojoCodecRegistry);
        collection = database.getCollection("TestResult");
    }

    @Override
    public void insertTestcaseResults(List<TestcaseResult> initialTestcaseResults) {
        initialTestcaseResults.forEach(this::insertTestcaseResult);
    }

    @Override
    public void insertTestcaseResult(TestcaseResult testcaseResult) {
        List<Document> expectations = testcaseResult.getCompletionPoints().stream()
                .map(expectation ->
                        new Document("key", expectation.getKey())
                                .append("status", expectation.getStatus().getValue()))
                .collect(Collectors.toList());
        Document document = new Document("_id", UUID.randomUUID().toString())
                .append("campaignId", testcaseResult.getCampaignId())
                .append("testcaseId", testcaseResult.getTestcaseId())
                .append("completionPoints", expectations)
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
            // TODO Handle update unexpected result
            log.error("Trying to update status of testcase {}.{} to {}. Expect modified count is 1 but encounter {}",
                    campaignId, testcaseId, status, updateResult.getModifiedCount());
        }
    }

    @Override
    public void updateExpectationStatus(String campaignId, String testcaseId,
                                        String expectationKey, TestcaseResult.CompletionPoint.Status status) {
        UpdateResult updateResult = collection.updateOne(
                Filters.and(Filters.eq("campaignId", campaignId), Filters.eq("testcaseId", testcaseId)),
                new Document("$set", new Document("completionPoints.$[element].status", status.getValue())),
                new UpdateOptions().arrayFilters(Arrays.asList(Filters.in("element.key", expectationKey)))
        );
        if (updateResult.getModifiedCount() != 1) {
            // TODO Handle update unexpected result
//            log.error("Trying to update status of expectation {}.{}.{} to {}. Expect modified count is 1 but encounter {}",
//                    campaignId, testcaseId, expectationKey, status, updateResult.getModifiedCount());
        }
    }

    @Override
    public List<TestcaseResult> findAllByCampaignIdAndStatusReady(String campaignId) {
        FindIterable<TestcaseResult> iterable = collection
                .withDocumentClass(TestcaseResult.class)
                .find(Filters.and(
                        new Document("campaignId", campaignId),
                        new Document("status", TestcaseResult.Status.PRECONDITIONS_READY.getValue()))
                );
        MongoCursor<TestcaseResult> iterator = iterable.iterator();
        List<TestcaseResult> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
    }
}
