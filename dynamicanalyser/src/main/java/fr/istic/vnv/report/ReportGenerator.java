package fr.istic.vnv.report;

import fr.istic.vnv.analysis.AnalysisContext;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public abstract class ReportGenerator {
    private AnalysisContext context;
    private File saveFolder;

    public ReportGenerator(File saveFolder) throws IOException {
        context = AnalysisContext.getAnalysisContext();
        this.saveFolder = saveFolder;

        if(!saveFolder.exists() && !saveFolder.mkdir() ) {
            FileUtils.forceMkdir(saveFolder);

            if(!saveFolder.exists())
                throw new IOException("Unable to create directory: " + saveFolder.getAbsolutePath());
        }
    }

    public AnalysisContext getContext() {
        return context;
    }

    public File getSaveFolder() {
        return saveFolder;
    }

    /**
     * This method can be called several times and each time it save all the execution trace into file and free memory.
     */
    public abstract void saveExecutionTraceUntilThen();
    public abstract void save();
}
