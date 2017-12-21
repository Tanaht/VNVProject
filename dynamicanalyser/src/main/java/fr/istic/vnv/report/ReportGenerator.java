package fr.istic.vnv.report;

import fr.istic.vnv.analysis.AnalysisContext;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public abstract class ReportGenerator {
    private AnalysisContext context;
    private File saveFolder;

    /**
     *
     * @param saveFolder path to save report
     * @throws IOException Append when the path file doesn't exist
     */
    public ReportGenerator(File saveFolder) throws IOException {
        context = AnalysisContext.getAnalysisContext();
        this.saveFolder = saveFolder;

        if(!saveFolder.exists() && !saveFolder.mkdir() ) {
            FileUtils.forceMkdir(saveFolder);

            if(!saveFolder.exists())
                throw new IOException("Unable to create directory: " + saveFolder.getAbsolutePath());
        }
    }

    /**
     * Return the context analyse
     * @return The context analyse
     */
    public AnalysisContext getContext() {
        return context;
    }

    /**
     * Return the path where to save the report
     * @return The path where to save the report
     */
    public File getSaveFolder() {
        return saveFolder;
    }

    /**
     * This method can be called several times and each time it save all the execution trace into file and free memory.
     */
    public abstract void saveExecutionTraceUntilThen();

    /**
     * Save the Report
     */
    public abstract void save();
}
