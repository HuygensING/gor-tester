package nl.knaw.huygens.gortester;

import com.google.common.io.Files;
import nl.knaw.huygens.gortester.differs.Differ;
import nl.knaw.huygens.gortester.messages.GorMessage;
import nl.knaw.huygens.gortester.messages.GorOriginalResponse;
import nl.knaw.huygens.gortester.messages.GorReplayedResponse;
import nl.knaw.huygens.gortester.messages.GorRequest;
import nl.knaw.huygens.gortester.rewriterules.RewriteRule;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class GorTester {

  private final Map<String, GorReplayedResponse> fastReplayedResponses;
  private final Map<String, GorOriginalResponse> originalResponses;
  private final Map<String, GorRequest> requests;
  private final String dirname;
  private final List<RewriteRule> rewriteRules;
  private final List<Differ> differs;

  public GorTester(Config config) {
    this.dirname = config.getOutputDir();
    this.rewriteRules = config.getRewriteRules();
    fastReplayedResponses = new HashMap<>();
    originalResponses = new HashMap<>();
    requests = new HashMap<>();
    differs = config.getDiffers();
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
    try {
      Files.write(
        statement.getHttpBlock(results),
        new File(dirname + File.separator + request.getId() + "_unmodifiedReplay.txt")
      );
    } catch (IOException e) {
      e.printStackTrace(results);
    }
    for (RewriteRule rewriteRule : rewriteRules) {
      rewriteRule.handleReplayResponse(request, originalResponse, statement);
    }
    requests.remove(statement.getId());
    compare(request, originalResponse, statement, results);
  }

  private void compare(GorRequest request, GorOriginalResponse orig, GorReplayedResponse replay, PrintWriter results) {
    AtomicBoolean headerWritten = new AtomicBoolean(false);
    AtomicBoolean isDifferent = new AtomicBoolean(false);
    for (Differ differ : this.differs) {
      if (differ.accept(request, orig)) {
        differ.diff(orig, replay).ifPresent(error -> {
          isDifferent.set(true);
          if (!headerWritten.getAndSet(true)) {
            results.println(replay.getId() + " " + request.getPath());
          }
          for (String line : error.split("\n")) {
            results.println("  " + line);
          }
        });
      }
    }

    if (isDifferent.get()) {
      writeToFile(request, orig, replay, results);
    }
  }

  private void writeToFile(GorRequest request, GorOriginalResponse orig, GorReplayedResponse replayed, PrintWriter
    results) {
    try {
      String slash = File.separator;
      Files.write(request.getHttpBlock(results), new File(dirname + slash + request.getId() + "_request.txt"));
      Files.write(orig.getHttpBlock(results), new File(dirname + slash + request.getId() + "_origResponse.txt"));
      Files.write(replayed.getHttpBlock(results), new File(dirname + slash + request.getId() + "_modifiedReplay.txt"));
    } catch (IOException e) {
      e.printStackTrace(results);
    }
  }
}
