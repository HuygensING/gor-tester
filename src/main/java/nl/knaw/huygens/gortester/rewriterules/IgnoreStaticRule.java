package nl.knaw.huygens.gortester.rewriterules;

import nl.knaw.huygens.gortester.messages.GorOriginalResponse;
import nl.knaw.huygens.gortester.messages.GorReplayedResponse;
import nl.knaw.huygens.gortester.messages.GorRequest;

import java.io.PrintWriter;

public class IgnoreStaticRule implements RewriteRule {

  private PrintWriter result;

  @Override
  public void setOutputWriter(PrintWriter outputWriter) {
    result = outputWriter;
  }

  @Override
  public void modifyRequestForReplay(GorRequest request) {

  }

  @Override
  public boolean blockReplay(GorRequest request) {
    if (request.getPath().startsWith("/static")) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void handleOriginalResponse(GorRequest request, GorOriginalResponse response) {

  }

  @Override
  public void handleReplayResponse(GorRequest request, GorOriginalResponse originalResponse,
                                   GorReplayedResponse response) {

  }
}
