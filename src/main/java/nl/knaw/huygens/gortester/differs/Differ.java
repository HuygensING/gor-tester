package nl.knaw.huygens.gortester.differs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import nl.knaw.huygens.gortester.Accepter;
import nl.knaw.huygens.gortester.messages.GorOriginalResponse;
import nl.knaw.huygens.gortester.messages.GorReplayedResponse;
import nl.knaw.huygens.gortester.messages.GorRequest;

import java.util.Optional;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
public abstract class Differ {
  private final Accepter accepter;

  @JsonCreator
  protected Differ(@JsonProperty("applyIf") Accepter accepter) {
    this.accepter = accepter;
  }

  public boolean accept(GorRequest request, GorOriginalResponse orig) {
    return accepter == null || accepter.accept(request, orig);
  }

  public abstract Optional<String> diff(GorOriginalResponse orig, GorReplayedResponse replay);

}
