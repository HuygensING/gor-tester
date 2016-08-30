package nl.knaw.huygens.gortester.rewriterules;

import nl.knaw.huygens.gortester.messages.GorOriginalResponse;
import nl.knaw.huygens.gortester.messages.GorReplayedResponse;
import nl.knaw.huygens.gortester.messages.GorRequest;

public interface RewriteRule {
  void modifyRequestForReplay(GorRequest request);

  boolean blockReplay(GorRequest request);

  void handleOriginalResponse(GorRequest request, GorOriginalResponse response);

  void handleReplayResponse(GorRequest request, GorOriginalResponse originalResponse, GorReplayedResponse response);
}
