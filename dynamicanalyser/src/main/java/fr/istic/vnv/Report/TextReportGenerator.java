package fr.istic.vnv.Report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;

public class TextReportGenerator extends ReportGenerator {
    private Logger log = LoggerFactory.getLogger(TextReportGenerator.class);

    @Override
    public void save(PrintStream stream) {
        stream.println("Branch Coverage:");
        stream.println("==================");

        for(String className : super.getContext().getClassContexts().keySet()) {
            stream.println(super.getContext().getClassContext(className));
        }

        stream.println();
        stream.println("Execution Trace:");
        stream.println("==================");

        for(String str : super.getContext().getExecutionTrace()) {

            if(str.startsWith("[START]")) {
                stream.println(str);
            } else if(str.startsWith("[END]")) {
                stream.println(str);
            }
        }
    }
}
