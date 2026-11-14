package org.zbaudoin.filesimilarity.application.monitoring;

import org.zbaudoin.filesimilarity.domain.config.AppConfig;
import org.zbaudoin.filesimilarity.domain.monitoring.MonitoringConfig;

import org.junit.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class MonitoringTest {

    @Test
    public void cancelIsSafeBeforeStartAndCanBeCalledRepeatedly() throws Exception {
        Path directory = Files.createTempDirectory("ressemblance-monitoring");
        try {
            Monitoring monitoring = new Monitoring(Logger.getLogger(Monitoring.class.getName()));

            monitoring.cancel();
            monitoring.start(new MonitoringConfig(directory.toString(), 100), new AppConfig());
            monitoring.cancel();
            monitoring.cancel();
        } finally {
            Files.deleteIfExists(directory);
        }
    }
}
