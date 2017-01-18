package nl.knaw.huygens.gortester.rewriterules;

import nl.knaw.huygens.gortester.messages.GorOriginalResponse;
import nl.knaw.huygens.gortester.messages.GorReplayedResponse;
import nl.knaw.huygens.gortester.messages.GorRequest;
import nl.knaw.huygens.gortester.messages.GorResponse;

import java.io.PrintWriter;
import java.util.Arrays;

public class UnchunkRule implements RewriteRule {

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
    return false;
  }

  @Override
  public void handleOriginalResponse(GorRequest request, GorOriginalResponse response) {
    unchunkBody(response);
  }

  @Override
  public void handleReplayResponse(GorRequest request, GorOriginalResponse originalResponse,
                                   GorReplayedResponse response) {
    unchunkBody(response);
  }

  private void unchunkBody(GorResponse response) {
    response.getHeader("Transfer-Encoding").ifPresent(encoding -> {
      if (encoding.equals("chunked")) {
        byte[] body = response.getBody();
        byte[] result = new byte[body.length]; //we'll truncate at the end
        int resultPos = 0;
        String chunksizeStr = "";
        for (int i = 0; i < body.length; i++) {
          if (body[i] == '\r') {

            int chunksize = Integer.parseInt(chunksizeStr, 16);
            i += 2; //skip the \r\n (position i at at the first char of the chunk)
            System.arraycopy(body, i, result, resultPos, chunksize); //write to result
            resultPos += chunksize;
            i += chunksize + 1; //position i at the \n after the chunk
            chunksizeStr = "";
          } else {
            chunksizeStr += (char)body[i];
          }
        }
        response.setBody(Arrays.copyOf(result, resultPos));
        response.removeHeader("Transfer-Encoding");
      }
    });
  }

}
