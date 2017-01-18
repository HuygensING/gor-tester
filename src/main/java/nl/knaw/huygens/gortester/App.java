package nl.knaw.huygens.gortester;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import nl.knaw.huygens.gortester.messages.GorMessage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

public class App {

  public static void main( String[] args ) throws FileNotFoundException, UnsupportedEncodingException {
    if (args.length == 0) {
      System.err.println("You must specify a config file");
      System.exit(1);
      return;
    }
    Scanner sc = new Scanner(System.in);
    PrintStream out = System.out;
    Config config;
    try {
      config = new ObjectMapper(new YAMLFactory()).readValue(new File(args[0]), Config.class);
    } catch (IOException e) {
      e.printStackTrace(System.err);
      System.exit(2);
      return;
    }
    String dirname = config.getOutputDir();
    File outputDir = new File(dirname);
    if (!outputDir.mkdirs()) {
      if (!outputDir.exists()) {
        System.err.println("Could not create output dir");
        System.exit(3);
        return;
      }
    }
    try (PrintWriter results = new PrintWriter(dirname + File.separator + "log", "UTF-8")) {
      GorTester gorTester = new GorTester(config);
      while (sc.hasNext()) {
        try {
          GorMessage statement = GorMessage.make(sc.nextLine(), results);
          gorTester.handleLine(out, results, statement);
        } catch (Exception e) {
          e.printStackTrace(results);
        } finally {
          results.flush();
        }
      }
    }
  }

}
