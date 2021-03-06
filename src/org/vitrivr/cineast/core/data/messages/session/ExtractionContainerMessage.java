package org.vitrivr.cineast.core.data.messages.session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.core.data.messages.interfaces.Message;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.run.ExtractionItemContainer;

/**
 * @author silvan on 22.01.18.
 */
public class ExtractionContainerMessage implements Message {

  private ExtractionItemContainer[] items;

  @JsonCreator
  public ExtractionContainerMessage(@JsonProperty("items") ExtractionItemContainer[] items) {
    this.items = items;
  }

  public ExtractionItemContainer[] getItems() {
    return this.items;
  }

  @Override
  public MessageType getMessageType() {
    return null;
  }
}
