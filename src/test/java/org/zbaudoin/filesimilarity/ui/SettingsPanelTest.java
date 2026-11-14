package org.zbaudoin.filesimilarity.ui;


import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SettingsPanelTest {

    @Test
    public void coefficientMustBeBetweenZeroAndOneInclusive() {
        assertTrue(SettingsPanel.isValidCoefficient("0"));
        assertTrue(SettingsPanel.isValidCoefficient("1"));
        assertTrue(SettingsPanel.isValidCoefficient("0.5"));
        assertFalse(SettingsPanel.isValidCoefficient("-0.01"));
        assertFalse(SettingsPanel.isValidCoefficient("1.01"));
        assertFalse(SettingsPanel.isValidCoefficient("NaN"));
        assertFalse(SettingsPanel.isValidCoefficient("Infinity"));
    }
}
