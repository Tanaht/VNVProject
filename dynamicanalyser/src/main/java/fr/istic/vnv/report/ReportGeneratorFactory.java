package fr.istic.vnv.report;

import java.io.File;
import java.io.IOException;

public class ReportGeneratorFactory {

    public static ReportGenerator getTextReportGenerator(File file) throws IOException {
        return new TextReportGenerator(file);
    }
}
