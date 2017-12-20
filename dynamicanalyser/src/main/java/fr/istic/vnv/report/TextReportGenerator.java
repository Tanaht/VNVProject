package fr.istic.vnv.report;

import fr.istic.vnv.utils.Config;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

public class TextReportGenerator extends ReportGenerator {
    private Logger log = LoggerFactory.getLogger(TextReportGenerator.class);
    private int executionTraceCounter;

    public TextReportGenerator(File saveFolder) throws IOException {
        super(saveFolder);
    }

    private void saveBranchCoverage(PrintStream stream) {
        log.info("Generating report for Branch Coverage Details...");
        stream.println("Branch Coverage:");
        stream.println("==================");

        for(String className : super.getContext().getClassContexts().keySet()) {
            stream.println(super.getContext().getClassContext(className));
        }

        stream.close();
    }

    public void saveExecutionTraceUntilThen() {
        File executionTraceFragment = FileUtils.getFile(super.getSaveFolder(), "VNVReport-ExecutionTrace." + ++executionTraceCounter + ".txt");
        try {
            executionTraceFragment.createNewFile();
            PrintWriter stream = new PrintWriter(new BufferedWriter(new FileWriter(executionTraceFragment)));
            log.info("Generating report n°{} for Execution Trace Details... (it can take a few minutes)", executionTraceCounter);
            stream.println("Execution Trace:");
            stream.println("==================");

            List<String> executionTrace = super.getContext().getExecutionTrace();

            while(!executionTrace.isEmpty()) {
                stream.println(executionTrace.remove(0));
                if(executionTrace.size() % 100000 == 0) {
                    log.trace("Remain execution traces to store into {}: {}", executionTraceFragment.getAbsolutePath(), executionTrace.size());
                }
            }

            stream.close();

        } catch(IOException e) {
            log.error(e.getMessage());
        }
        log.trace("Execution Trace have been saved correctly into {}", executionTraceFragment.getAbsolutePath());
    }

    @Override
    public void save() {

        if(Config.get().doBranchCoverage()) {
            try {
                File branchCoverageReport = FileUtils.getFile(super.getSaveFolder(), "VNVReport-BranchCoverage.txt");
                branchCoverageReport.createNewFile();
                saveBranchCoverage(new PrintStream(branchCoverageReport));
            } catch (IOException e) {
                log.error("Unable to save Branch Coverage report: " + e.getMessage());
            }
        }

        if(!Config.get().doExecutionTrace())
            return;

        saveExecutionTraceUntilThen();
    }
}
