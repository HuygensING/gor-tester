package nl.knaw.huygens.gortester.rewriterules;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.gortester.messages.GorOriginalResponse;
import nl.knaw.huygens.gortester.messages.GorReplayedResponse;
import nl.knaw.huygens.gortester.messages.GorRequest;

import java.io.PrintWriter;

public class BlockReplayRule implements RewriteRule {

  private final String pathRegex;
  private PrintWriter result;

  @JsonCreator
  private BlockReplayRule(@JsonProperty("pathMatches") String pathRegex) {
    this.pathRegex = pathRegex;
  }

  @Override
  public void setOutputWriter(PrintWriter outputWriter) {
    result = outputWriter;
  }

  @Override
  public void modifyRequestForReplay(GorRequest request) {

  }

  @Override
  public boolean blockReplay(GorRequest request) {
    return request.getPath().matches(pathRegex);
  }

  @Override
  public void handleOriginalResponse(GorRequest request, GorOriginalResponse response) {

  }

  @Override
  public void handleReplayResponse(GorRequest request, GorOriginalResponse originalResponse,
                                   GorReplayedResponse response) {

  }
}
