package nl.knaw.huygens.gortester.differs;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.gortester.Accepter;
import nl.knaw.huygens.gortester.messages.GorOriginalResponse;
import nl.knaw.huygens.gortester.messages.GorReplayedResponse;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class HeaderDiffer extends Differ {

  private static final String CAPTION = "had different headers:\n";
  private final Set<String> headersToIgnore;

  protected HeaderDiffer(
    @JsonProperty("criteria") Accepter accepter,
    @JsonProperty("ignoreHeader") Set<String> headersToIgnore) {
    super(accepter);
    this.headersToIgnore = headersToIgnore;
  }

  @Override
  public Optional<String> diff(GorOriginalResponse orig, GorReplayedResponse replay) {
    String result = CAPTION;
    for (Map.Entry<String, String> entry : orig.getHeaderList()) {
      if (headersToIgnore.contains(entry.getKey())) {
        continue;
      }
      Optional<String> replayedHeader = replay.getHeader(entry.getKey());
      if (replayedHeader.isPresent()) {
        if (!replayedHeader.get().equals(entry.getValue())) {
          result += "  " + entry.getKey() + ": " + entry.getValue() + " - " + replayedHeader.get() + "\n";
        }
      } else {
        result += "  " + entry.getKey() + ": " + entry.getValue() + " - \n";
      }
    }
    //replayed is allowed to have more headers.
    if (result.length() > CAPTION.length()) {
      return Optional.of(result);
    } else {
      return Optional.empty();
    }
  }
}
