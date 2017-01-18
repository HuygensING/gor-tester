package nl.knaw.huygens.gortester;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.gortester.messages.GorOriginalResponse;
import nl.knaw.huygens.gortester.messages.GorRequest;

import java.util.Map;

public class Accepter {
  private final Map<String, String> headerMatchesRegex;
  private final String urlMatchesRegex;

  public boolean accept(GorRequest request, GorOriginalResponse response) {
    if (urlMatchesRegex != null) {
      if (!request.getPath().matches(urlMatchesRegex)) {
        return false;
      }
    }
    if (headerMatchesRegex != null) {
      for (Map.Entry<String, String> header : response.getHeaderList()) {
        if (headerMatchesRegex.containsKey(header.getKey())) {
          if (!header.getValue().matches(headerMatchesRegex.get(header.getKey()))) {
            return false;
          }
        }
      }
    }
    return true;
  }

  @JsonCreator
  public Accepter(@JsonProperty("headerMatches") Map<String, String> headerMatchesRegex,
                  @JsonProperty("urlMatches") String urlMatchesRegex) {

    if (headerMatchesRegex != null && !headerMatchesRegex.isEmpty()) {
      this.headerMatchesRegex = headerMatchesRegex;
    } else {
      this.headerMatchesRegex = null;
    }
    if (urlMatchesRegex != null && urlMatchesRegex.length() > 0) {
      this.urlMatchesRegex = urlMatchesRegex;
    } else {
      this.urlMatchesRegex = null;
    }
  }
}
