package nl.knaw.huygens.gortester.rewriterules;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import nl.knaw.huygens.gortester.messages.GorOriginalResponse;
import nl.knaw.huygens.gortester.messages.GorReplayedResponse;
import nl.knaw.huygens.gortester.messages.GorRequest;

import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;

public class StoreAuthRule implements RewriteRule {

  private PrintWriter result;

  @Override
  public void setOutputWriter(PrintWriter outputWriter) {
    result = outputWriter;
  }

  private final Cache<String, String> auths;

  @JsonCreator
  public StoreAuthRule(@JsonProperty("maxSize") long maxSize) {
    auths = CacheBuilder.newBuilder().maximumSize(maxSize).build();
  }

  @Override
  public void modifyRequestForReplay(GorRequest statement) {
    statement.replaceHeader("AUTH_TOKEN", token -> {
      try {
        return auths.get(token, () -> token);
      } catch (ExecutionException e) {
        return token; //ignore exception
      }
    });
  }

  @Override
  public boolean blockReplay(GorRequest statement) {
    return false;
  }

  @Override
  public void handleOriginalResponse(GorRequest request, GorOriginalResponse response) {
  }

  @Override
  public void handleReplayResponse(GorRequest request, GorOriginalResponse originalResponse,
                                   GorReplayedResponse response) {
    if (request.getPath().equals("/v2.1/authenticate")) {
      response.getHeader("Authorization").ifPresent(authHeader ->
        originalResponse.getHeader("Authorization").ifPresent(origHeader ->
          auths.put(origHeader, authHeader)
        )
      );
    }
  }
}
