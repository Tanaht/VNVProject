package fr.istic.vnv.report;

import fr.istic.vnv.analysis.ClassContext;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;

public class TestReportGeneratorFactory {
    @Test
    void getTextReportGenerator_can_not_return_null() throws IOException {
        assertThat(ReportGeneratorFactory.getTextReportGenerator(new File("src")),any(ReportGenerator.class));
    }

}
