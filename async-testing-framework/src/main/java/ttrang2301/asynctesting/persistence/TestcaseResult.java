package ttrang2301.asynctesting.persistence;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestcaseResult {

    private String campaignId;
    private String testcaseId;
    private List<CompletionPoint> completionPoints;
    private Status status;

    public enum  Status {
        INITIALIZED("Initialized"), PRECONDITIONS_READY("Ready"), SUCCESSFUL("Successful"), FAILED("Failed");

        private String value;

        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Status fromString(String string) {
            for (Status status : Status.values()) {
                if (Objects.equal(status.getValue(), string)) {
                    return status;
                }
            }
            throw new RuntimeException("Cannot convert text '" + string + "' to " + Status.class.getName());
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class CompletionPoint {

        private String key;
        private Status status;

        public enum Status {
            SUCCESSFUL("Successful"), UNKNOWN("Unknown");

            private String value;

            Status(String value) {
                this.value = value;
            }

            public String getValue() {
                return value;
            }

            public static Status fromString(String string) {
                for (Status status : Status.values()) {
                    if (Objects.equal(status.getValue(), string)) {
                        return status;
                    }
                }
                throw new RuntimeException("Cannot convert text '" + string + "' to " + Status.class.getName());
            }

        }

    }

    public static final class TestcaseStatusCodec implements Codec<Status> {

        @Override
        public Status decode(BsonReader bsonReader, DecoderContext decoderContext) {
            return Status.fromString(bsonReader.readString());
        }

        @Override
        public void encode(BsonWriter bsonWriter, Status status, EncoderContext encoderContext) {
            bsonWriter.writeString(status.getValue());
        }

        @Override
        public Class<Status> getEncoderClass() {
            return Status.class;
        }
    }

    public static final class CompletionPointStatusCodec implements Codec<CompletionPoint.Status> {

        @Override
        public CompletionPoint.Status decode(BsonReader bsonReader, DecoderContext decoderContext) {
            return CompletionPoint.Status.fromString(bsonReader.readString());
        }

        @Override
        public void encode(BsonWriter bsonWriter, CompletionPoint.Status status, EncoderContext encoderContext) {
            bsonWriter.writeString(status.getValue());
        }

        @Override
        public Class<CompletionPoint.Status> getEncoderClass() {
            return CompletionPoint.Status.class;
        }
    }

}
