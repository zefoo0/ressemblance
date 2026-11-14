package org.zbaudoin.filesimilarity.infrastructure.config;

import org.zbaudoin.filesimilarity.domain.config.AppConfig;
import org.zbaudoin.filesimilarity.domain.comparison.ComparisonStrategy;
import org.zbaudoin.filesimilarity.infrastructure.comparison.strategy.FileContentComparisonStrategy;
import org.zbaudoin.filesimilarity.infrastructure.comparison.strategy.FileNameComparisonStrategy;
import org.zbaudoin.filesimilarity.infrastructure.comparison.strategy.ImageContentComparisonStrategy;
import org.zbaudoin.filesimilarity.infrastructure.comparison.strategy.VideoContentComparisonStrategy;

import java.util.Arrays;

public final class DefaultAppConfigFactory {
    private DefaultAppConfigFactory() {
    }

    public static AppConfig create() {
        AppConfig config = new AppConfig();
        config.setComparators(Arrays.<ComparisonStrategy>asList(
                new FileContentComparisonStrategy(),
                new ImageContentComparisonStrategy(),
                new VideoContentComparisonStrategy(),
                new FileNameComparisonStrategy()));
        return config;
    }
}
