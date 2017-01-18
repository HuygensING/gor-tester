package nl.knaw.huygens.gortester;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.gortester.differs.Differ;
import nl.knaw.huygens.gortester.rewriterules.RewriteRule;

import java.util.LinkedList;
import java.util.List;

public class Config {
  @JsonProperty
  private String outputDir = "output";

  @JsonProperty
  private List<Differ> differs = new LinkedList<>();

  @JsonProperty
  private List<RewriteRule> rewriteRules = new LinkedList<>();

  public String getOutputDir() {
    return outputDir;
  }

  public List<RewriteRule> getRewriteRules() {
    return rewriteRules;
  }

  public List<Differ> getDiffers() {
    return differs;
  }
}
