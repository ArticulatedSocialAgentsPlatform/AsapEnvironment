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
package hmi.audioenvironment;


/**
 * Interface for the playback of (wav) audio
 * @author Herwin van Welbergen
 * @author Dennis Reidsma
 *
 */
public interface Wav
{

    float getVolume();
    void setVolume(float vol);
    /**
     * @param relTime time relative to the start of the Wav
     */
    void start(double relTime);
    
    /**
     * Stops and cleans up the Wav
     */
    void stop();
    
    /**
     * Play
     * @param relTime relative to start of Wav
     */
    void play(double relTime) throws WavPlayException;
    
    /**
     * Get the duration of the Wav in seconds
     * @return
     */
    double getDuration();
}
