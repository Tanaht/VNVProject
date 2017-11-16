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

        for(String str : super.getContext().getExecutionTrace())
            System.out.println(str);
    }

    /*private String printExecutionTrace(String tab, List<String> trace) {
        if(trace.size() < 1)
            return "";

        String current = trace.get(0);


        if(current.startsWith("[START]")) {
            return tab + current + '\n' + printExecutionTrace(tab + '\t', trace.subList(1, trace.size()));
        } else if(current.startsWith("[END]")) {
            log.info("tab size {}", tab.length());
            tab = tab.substring(1);
            return tab + current + '\n' + printExecutionTrace(tab, trace.subList(1, trace.size()));
        }

        throw new RuntimeException("Should never be executed");
    }*/
}
