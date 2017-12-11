package fr.istic.vnv.report;

import fr.istic.vnv.analysis.AnalysisContext;

import java.io.PrintStream;

public abstract class ReportGenerator {
    private AnalysisContext context;

    public ReportGenerator() {
        context = AnalysisContext.getAnalysisContext();
    }

    public AnalysisContext getContext() {
        return context;
    }

    public abstract void save(PrintStream stream);
}
