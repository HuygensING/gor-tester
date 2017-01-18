package nl.knaw.huygens.gortester.differs;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.gortester.Accepter;
import nl.knaw.huygens.gortester.messages.GorOriginalResponse;
import nl.knaw.huygens.gortester.messages.GorReplayedResponse;

import java.util.Arrays;
import java.util.Optional;

public class BinaryBodyDiffer extends Differ {
  protected BinaryBodyDiffer(@JsonProperty("criteria") Accepter accepter) {
    super(accepter);
  }

  @Override
  public Optional<String> diff(GorOriginalResponse orig, GorReplayedResponse replay) {
    if (Arrays.equals(orig.getBody(), replay.getBody())) {
      return Optional.empty();
    } else {
      return Optional.of("had a different response body");
    }
  }
}
