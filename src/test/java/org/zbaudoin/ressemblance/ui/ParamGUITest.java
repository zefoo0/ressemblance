package org.zbaudoin.ressemblance.ui;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ParamGUITest {

    @Test
    public void coefficientMustBeBetweenZeroAndOneInclusive() {
        assertTrue(ParamGUI.isValidCoefficient("0"));
        assertTrue(ParamGUI.isValidCoefficient("1"));
        assertTrue(ParamGUI.isValidCoefficient("0.5"));
        assertFalse(ParamGUI.isValidCoefficient("-0.01"));
        assertFalse(ParamGUI.isValidCoefficient("1.01"));
        assertFalse(ParamGUI.isValidCoefficient("NaN"));
        assertFalse(ParamGUI.isValidCoefficient("Infinity"));
    }
}
