package nl.knaw.huygens.gortester.rewriterules;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import nl.knaw.huygens.gortester.messages.GorOriginalResponse;
import nl.knaw.huygens.gortester.messages.GorReplayedResponse;
import nl.knaw.huygens.gortester.messages.GorRequest;

import java.io.PrintWriter;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
public interface RewriteRule {

  @JsonIgnore
  void setOutputWriter(PrintWriter outputWriter);

  void modifyRequestForReplay(GorRequest request);

  boolean blockReplay(GorRequest request);

  void handleOriginalResponse(GorRequest request, GorOriginalResponse response);

  void handleReplayResponse(GorRequest request, GorOriginalResponse originalResponse, GorReplayedResponse response);
}
