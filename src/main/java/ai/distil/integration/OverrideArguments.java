package ai.distil.integration;

import com.beust.jcommander.Parameter;
import org.springframework.stereotype.Component;

@Component
public class OverrideArguments {

    @Parameter(names = {"-h", "--help"}, description = "Whether to show help info")
    private boolean helpMode = false;

    @Parameter(names = {"-mds", "--max-data-sync"}, description = "The amount of data to limit each data set sync to")
    private int maxDataSourceSize = -1;

    public boolean isHelpMode() {
        return helpMode;
    }

    public int getMaxDataSourceSize() {
        return maxDataSourceSize;
    }
}
