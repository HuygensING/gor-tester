package nl.knaw.huygens.gortester.rewriterules;

import com.google.common.io.ByteStreams;
import nl.knaw.huygens.gortester.messages.GorOriginalResponse;
import nl.knaw.huygens.gortester.messages.GorReplayedResponse;
import nl.knaw.huygens.gortester.messages.GorRequest;
import nl.knaw.huygens.gortester.messages.GorResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.GZIPInputStream;

public class GunzipRule implements RewriteRule {

  private final PrintWriter result;

  public GunzipRule(PrintWriter result) {
    this.result = result;
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
    unzipBody(response);
  }

  @Override
  public void handleReplayResponse(GorRequest request, GorOriginalResponse originalResponse,
                                   GorReplayedResponse response) {
    unzipBody(response);
  }

  private void unzipBody(GorResponse response) {
    response.getHeader("Content-Encoding").ifPresent(encoding -> {
      if (encoding.equals("gzip")) {
        try {
          response.setBody(ByteStreams.toByteArray(new GZIPInputStream(new ByteArrayInputStream(response.getBody()))));
        } catch (IOException e) {
          e.printStackTrace(result);
        }
        response.removeHeader("Content-Encoding");
      }
    });
  }
}
