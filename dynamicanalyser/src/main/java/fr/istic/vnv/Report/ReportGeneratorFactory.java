package fr.istic.vnv.Report;

public class ReportGeneratorFactory {

    public static ReportGenerator getTextReportGenerator() {
        return new TextReportGenerator();
    }
}
