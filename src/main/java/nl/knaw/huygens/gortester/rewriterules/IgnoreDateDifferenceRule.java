package nl.knaw.huygens.gortester.rewriterules;

import nl.knaw.huygens.gortester.messages.GorOriginalResponse;
import nl.knaw.huygens.gortester.messages.GorReplayedResponse;
import nl.knaw.huygens.gortester.messages.GorRequest;

public class IgnoreDateDifferenceRule implements RewriteRule {
  @Override
  public void modifyRequestForReplay(GorRequest request) {

  }

  @Override
  public boolean blockReplay(GorRequest request) {
    return false;
  }

  @Override
  public void handleOriginalResponse(GorRequest request, GorOriginalResponse response) {

  }

  @Override
  public void handleReplayResponse(GorRequest request, GorOriginalResponse originalResponse,
                                   GorReplayedResponse response) {
    originalResponse.getHeader("Date").ifPresent(origDate -> response.replaceHeader("Date", replayDate -> origDate));
  }
}
