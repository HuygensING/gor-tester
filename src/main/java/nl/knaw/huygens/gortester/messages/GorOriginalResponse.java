package nl.knaw.huygens.gortester.messages;

import java.io.PrintWriter;

public class GorOriginalResponse extends GorResponse {
  public GorOriginalResponse(String line, byte[] gorSpec, String header, PrintWriter results) {
    super(line, gorSpec, header, results);
  }
}
