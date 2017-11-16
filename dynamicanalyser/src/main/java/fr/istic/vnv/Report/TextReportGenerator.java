package fr.istic.vnv.Report;

import java.io.PrintStream;

public class TextReportGenerator extends ReportGenerator {

    @Override
    public void save(PrintStream stream) {
        stream.println("Execution Trace:");
        stream.println("==================");

        for(String trace : super.getContext().getExecutionTrace()) {
            stream.println(trace);
        }
    }
}
