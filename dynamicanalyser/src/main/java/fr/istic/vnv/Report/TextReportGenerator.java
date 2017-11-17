package fr.istic.vnv.Report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;

public class TextReportGenerator extends ReportGenerator {
    private Logger log = LoggerFactory.getLogger(TextReportGenerator.class);

    @Override
    public void save(PrintStream stream) {
        stream.println("Execution Trace:");
        stream.println("==================");

        String tabs = "";
        for(String str : super.getContext().getExecutionTrace()) {

            if(str.startsWith("[START]")) {
                stream.println(tabs + str);
                tabs += "\t";
            } else if(str.startsWith("[END]")) {
                tabs = tabs.substring(1);
                stream.println(tabs + str);
            }
        }
    }
}
