package org.vitrivr.cineast.core.data.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageType;

import static org.vitrivr.cineast.core.data.messages.interfaces.MessageType.Q_SEG;

public class SegmentQuery extends Query {

    /** ID of the {@link MediaSegmentDescriptor} which should be retrieved. */
    private final String segmentId;

    /**
     * Constructor for {@link SegmentQuery} message.
     *
     * @param config The {@link QueryConfig}
     */
    @JsonCreator
    public SegmentQuery(@JsonProperty(value = "segmentId", required = true) String segmentId,
                        @JsonProperty(value = "config", required = false) QueryConfig config) {
        super(config);
        this.segmentId = segmentId;
    }

    /**
     * Getter for {@link SegmentQuery#segmentId}
     *
     * @return
     */
    public String getSegmentId() {
        return segmentId;
    }

    @Override
    public MessageType getMessageType() {
        return Q_SEG;
    }
}
