package nl.knaw.huygens.gortester.differs;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.gortester.Accepter;
import nl.knaw.huygens.gortester.messages.GorOriginalResponse;
import nl.knaw.huygens.gortester.messages.GorReplayedResponse;

import java.util.Optional;

public class StatusDiffer extends Differ {
  protected StatusDiffer(@JsonProperty("criteria") Accepter accepter) {
    super(accepter);
  }

  @Override
  public Optional<String> diff(GorOriginalResponse orig, GorReplayedResponse replay) {
    if (orig.getStatus() != replay.getStatus()) {
      return Optional.of("had a different status: " + orig.getStatus() + " " + replay.getStatus());
    } else {
      return Optional.empty();
    }
  }
}
