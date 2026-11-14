package org.zbaudoin.ressemblance.service;

import org.junit.Test;
import org.zbaudoin.ressemblance.model.MonitoringParam;
import org.zbaudoin.ressemblance.model.Param;

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
            monitoring.start(new MonitoringParam(directory.toString(), 100), new Param());
            monitoring.cancel();
            monitoring.cancel();
        } finally {
            Files.deleteIfExists(directory);
        }
    }
}
