package fr.istic.vnv.report;

import java.io.File;
import java.io.IOException;

public class ReportGeneratorFactory {

    /**
     * Return a ReportGenerator where the report is created to file
     * @param file path where is saved the report
     * @return ReportGenerator object
     * @throws IOException Append when the path file doesn't exist
     */
    public static ReportGenerator getTextReportGenerator(File file) throws IOException {
        return new TextReportGenerator(file);
    }
}
