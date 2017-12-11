package fr.istic.vnv.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;

public class TextReportGenerator extends ReportGenerator {
    private Logger log = LoggerFactory.getLogger(TextReportGenerator.class);

    @Override
    public void save(PrintStream stream) {
        log.info("Generating report for Branch Coverage Details...");
        stream.println("Branch Coverage:");
        stream.println("==================");

        for(String className : super.getContext().getClassContexts().keySet()) {
            stream.println(super.getContext().getClassContext(className));
        }

        stream.println();

        log.info("Generating report for Execution Trace Details... (it can take a few minutes)");
        stream.println("Execution Trace:");
        stream.println("==================");


        int size = super.getContext().getExecutionTrace().size();
        double percent = 0;
        for (int i = 0; i < size; i++) {
            String str = super.getContext().getExecutionTrace().get(i);

            double actualPercent = (i+1.0)/ size * 100.0;

            if(Math.floor(actualPercent) > Math.floor(percent)) {
                percent = actualPercent;

                if(!log.isDebugEnabled() && (
                        Math.floor(percent) == 1 ||
                        Math.floor(percent) == 25 ||
                        Math.floor(percent) == 50 ||
                        Math.floor(percent) == 75 ||
                        Math.floor(percent) == 100)
                ) {
                    log.info("Percent {} %", Math.floor(percent));
                }
                log.debug("Percent {} %", Math.floor(percent));
            }
            stream.println(str);

        }

        log.info("...Done");
    }
}
