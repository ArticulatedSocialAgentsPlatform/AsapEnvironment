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

import hmi.environmentbase.Embodiment;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
*/
@Slf4j
public class JComponentEmbodiment implements Embodiment
{

    private JComponent thePanel = null;
    
    public void setMasterComponent(JComponent newPanel)
    {
        thePanel = newPanel;
    }

    public void addJComponent(final JComponent jc)
    {
        SwingUtilities.invokeLater(new Runnable()
        {

            @Override
            public void run()
            {
                thePanel.add(jc);
                thePanel.revalidate();
                thePanel.repaint();
            }
        });

    }

    public void removeJComponent(final JComponent jc)
    {
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {

                @Override
                public void run()
                {
                    thePanel.remove(jc);
                    thePanel.revalidate();
                    thePanel.repaint();
                }

            });
        }
        catch (InvocationTargetException e)
        {
            log.warn("Exception in adding new JComponent", e);
        }
        catch (InterruptedException e)
        {
            log.warn("Exception in addJComponent initialization", e);
            Thread.interrupted();
        }
    }

    @Getter
    @Setter
    private String id = "jcomponentenvironment";
}
