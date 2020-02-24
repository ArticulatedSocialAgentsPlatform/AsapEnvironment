/*******************************************************************************
 * Copyright (C) 2009-2020 Human Media Interaction, University of Twente, the Netherlands
 *
 * This file is part of the Articulated Social Agents Platform BML realizer (ASAPRealizer).
 *
 * ASAPRealizer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ASAPRealizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ASAPRealizer.  If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package hmi.jcomponentenvironment;

import hmi.environmentbase.InputSwitchEmbodiment;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Combobox input selector for the VJointSwitchEmbodiment
 * @author hvanwelbergen
 *
 */
public class InputSwitchEmbodimentSwingUI 
{
    private JComboBox<String> switchBox;
    
    public JComponent getJComponent()
    {
        return switchBox;
    }
    
    public InputSwitchEmbodimentSwingUI(String id, final InputSwitchEmbodiment vjSwitch)
    {
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                public void run()
                {
                    switchBox = new JComboBox<>();
                    for (String input : vjSwitch.getInputs())
                    {
                        switchBox.addItem(input);
                    }
                    switchBox.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent e)
                        {
                            vjSwitch.selectInput(switchBox.getSelectedItem().toString());
                        }
                    });
                }
            });
        }
        catch (InvocationTargetException | InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }    
}
