/*
 *  Copyright 2006-2020 The MZmine Development Team
 *
 *  This file is part of MZmine.
 *
 *  MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 *  General Public License as published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version.
 *
 *  MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 *  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 *  Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with MZmine; if not,
 *  write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 *  USA
 */

package io.github.mzmine.datamodel.featuredata;

import io.github.mzmine.datamodel.MassSpectrum;
import io.github.mzmine.util.MemoryMapStorage;
import java.util.List;

/**
 * Stores combinations of intensity and mz values.
 *
 * @param <T>
 * @author https://github.com/SteffenHeu
 */
public interface IonSpectrumSeries<T extends MassSpectrum> extends IonSeries {

  List<T> getSpectra();

  default T getSpectrum(int index) {
    return getSpectra().get(index);
  }

  /**
   * @param spectrum
   * @return The intensity value for the given spectrum or 0 if the no intensity was measured at
   * that spectrum.
   */
  double getIntensityForSpectrum(T spectrum);

  /**
   * @param spectrum
   * @return The mz for the given spectrum or 0 if no intensity was measured at that spectrum.
   */
  default double getMzForSpectrum(T spectrum) {
    int index = getSpectra().indexOf(spectrum);
    if (index != -1) {
      return getMZ(index);
    }
    return 0;
  }

  /**
   * Creates a sub series of this series with the data corresponding to the given list of spectra.
   *
   * @param storage The new storage
   * @param subset  The subset of spectra.
   * @return The subset series.
   */
  IonSpectrumSeries<T> subSeries(MemoryMapStorage storage, List<T> subset);
}