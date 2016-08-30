package nl.knaw.huygens.gortester;

import com.google.common.io.Files;
import nl.knaw.huygens.gortester.messages.GorMessage;
import nl.knaw.huygens.gortester.messages.GorOriginalResponse;
import nl.knaw.huygens.gortester.messages.GorReplayedResponse;
import nl.knaw.huygens.gortester.messages.GorRequest;
import nl.knaw.huygens.gortester.rewriterules.RewriteRule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GorTester {

  private final Map<String, GorReplayedResponse> fastReplayedResponses;
  private final Map<String, GorOriginalResponse> originalResponses;
  private final Map<String, GorRequest> requests;
  private final String dirname;
  private final RewriteRule[] rewriteRules;

  public GorTester(String dirname, RewriteRule... rewriteRules) throws FileNotFoundException,
      UnsupportedEncodingException {
    this.dirname = dirname;
    this.rewriteRules = rewriteRules;
    fastReplayedResponses = new HashMap<>();
    originalResponses = new HashMap<>();
    requests = new HashMap<>();
  }

  public void handleLine(PrintStream out, PrintWriter results, GorMessage statement) {
    if (statement instanceof GorRequest ||
      statement instanceof GorReplayedResponse ||
      statement instanceof GorOriginalResponse) {
      if (statement instanceof GorRequest) {
        requests.put(statement.getId(), (GorRequest) statement);
        handleRequest(out, results, (GorRequest) statement);
      } else {
        out.println(statement.asLine());
        if (statement instanceof GorOriginalResponse) {
          handleOrigResponse((GorOriginalResponse) statement, results, requests.get(statement.getId()));
          //the replay response arrived before the original response (your code is getting faster! yeey!)
          if (fastReplayedResponses.containsKey(statement.getId())) {
            handleReplayedResponse(
              results,
              fastReplayedResponses.get(statement.getId()),
              (GorOriginalResponse) statement,
              requests
            );
            fastReplayedResponses.remove(statement.getId());
          } else {
            originalResponses.put(statement.getId(), (GorOriginalResponse) statement);
          }
        } else if (statement instanceof GorReplayedResponse) {
          if (originalResponses.containsKey(statement.getId())) {
            handleReplayedResponse(
              results,
              (GorReplayedResponse) statement,
              originalResponses.get(statement.getId()),
              requests
            );
            originalResponses.remove(statement.getId());
          } else {
            fastReplayedResponses.put(statement.getId(), (GorReplayedResponse) statement);
          }
        }
      }
    } else {
      throw new IllegalStateException("Unknown statement type");
    }
  }

  private void handleRequest(PrintStream requests, PrintWriter results, GorRequest statement) {
    for (RewriteRule rewriteRule : rewriteRules) {
      if (rewriteRule.blockReplay(statement)) {
        return;
      }
    }
    for (RewriteRule rewriteRule : rewriteRules) {
      rewriteRule.modifyRequestForReplay(statement);
    }
    requests.println(statement.asLine());
  }

  private void handleOrigResponse(GorOriginalResponse statement, PrintWriter results, GorRequest gorRequest) {
    for (RewriteRule rewriteRule : rewriteRules) {
      rewriteRule.handleOriginalResponse(gorRequest, statement);
    }
  }

  private void handleReplayedResponse(PrintWriter results, GorReplayedResponse statement, GorOriginalResponse
    originalResponse, Map<String, GorRequest> requests) {
    GorRequest request = requests.get(statement.getId());
    for (RewriteRule rewriteRule : rewriteRules) {
      rewriteRule.handleReplayResponse(request, originalResponse, statement);
    }
    requests.remove(statement.getId());
    compare(request, originalResponse, statement, results);
  }

  private void compare(GorRequest request, GorOriginalResponse orig, GorReplayedResponse replay, PrintWriter results) {
    boolean differs = false;
    results.println(replay.getId());

    if (orig.getStatus() != replay.getStatus()) {
      results.println("  had differing status");
      differs = true;
    }

    if (!Arrays.equals(orig.getBody(), replay.getBody())) {
      results.println("  had differing response bodies");
      differs = true;
    }

    for (Map.Entry<String, String> entry : orig.getHeaderList()) {
      Optional<String> replayedHeader = replay.getHeader(entry.getKey());
      if (replayedHeader.isPresent()) {
        if (!replayedHeader.get().equals(entry.getValue())) {
          results.println("  header differs. " + entry.getKey() + ": " + entry.getValue() + " - " +
            replayedHeader.get());
          differs = true;
        }
      } else {
        results.println("  replayed does not contain " + entry.getKey() + ": " + entry.getValue());
        differs = true;
      }
      //replayed is allowed to have more headers.
    }
    if (differs) {
      writeToFile(request, orig, replay, results);
    }
  }

  private void writeToFile(GorRequest request, GorOriginalResponse orig, GorReplayedResponse replayed, PrintWriter
    results) {
    try {
      String slash = File.separator;
      Files.write(request.getHttpBlock(results), new File(dirname + slash + request.getId() + "_req.txt"));
      Files.write(orig.getHttpBlock(results), new File(dirname + slash + request.getId() + "_orig.txt"));
      Files.write(replayed.getHttpBlock(results), new File(dirname + slash + request.getId() + "_repl.txt"));
    } catch (IOException e) {
      e.printStackTrace(results);
    }
  }
}
