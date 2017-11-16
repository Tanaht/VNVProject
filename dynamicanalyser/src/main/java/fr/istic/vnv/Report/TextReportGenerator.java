package fr.istic.vnv.Report;

import java.io.PrintStream;
import java.util.List;

public class TextReportGenerator extends ReportGenerator {

    @Override
    public void save(PrintStream stream) {
        stream.println("Execution Trace:");
        stream.println("==================");

        stream.println(printExecutionTrace("", super.getContext().getExecutionTrace()));
    }

    private String printExecutionTrace(String tab, List<String> trace) {
        if(trace.size() == 0)
            return "";

        String current = trace.get(0);


        if(current.startsWith("[START]")) {
            return tab + current + '\n' + printExecutionTrace(tab + '\t', trace.subList(1, trace.size()));
        } else if(current.startsWith("[END]")) {
            tab = tab.substring(1);
            return tab + current + '\n' + printExecutionTrace(tab, trace.subList(1, trace.size()));
        }

        throw new RuntimeException("Should never be executed");
    }
}
