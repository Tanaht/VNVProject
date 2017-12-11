package fr.istic.vnv.report;

public class ReportGeneratorFactory {

    public static ReportGenerator getTextReportGenerator() {
        return new TextReportGenerator();
    }
}
