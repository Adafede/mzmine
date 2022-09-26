/*
 * Copyright 2006-2022 The MZmine Development Team
 *
 * This file is part of MZmine.
 *
 * MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MZmine; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */
package io.github.mzmine.modules.visualization.spectra.spectra_stack.pseudospectra;

import io.github.mzmine.datamodel.identities.ms2.interf.AbstractMSMSDataPointIdentity;
import io.github.mzmine.datamodel.identities.ms2.interf.AbstractMSMSIdentity;
import io.github.mzmine.parameters.parametertypes.tolerances.MZTolerance;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class PseudoSpectrumDataSet extends XYSeriesCollection {

  private static final long serialVersionUID = 1L;

  private Map<XYDataItem, String> annotation;

  public PseudoSpectrumDataSet(boolean autoSort, Comparable... keys) {
    super();
    for (Comparable key : keys) {
      addSeries(new XYSeries(key, autoSort));
    }
  }

  public void addDP(double x, double y, String ann) {
    addDP(0, x, y, ann);
  }

  public void addDP(int series, double x, double y, String ann) {
    if (series >= getSeriesCount()) {
      throw new OutOfRangeException(series, 0, getSeriesCount());
    }

    XYDataItem dp = new XYDataItem(x, y);
    getSeries(series).add(dp);
    if (ann != null) {
      addAnnotation(dp, ann);
    }
  }

  /**
   * Add annotation
   *
   * @param dp
   * @param ann
   */
  public void addAnnotation(XYDataItem dp, String ann) {
    if (annotation == null) {
      this.annotation = new HashMap<>();
    }
    annotation.put(dp, ann);
  }

  public String getAnnotation(int series, int item) {
    if (annotation == null) {
      return null;
    }
    XYDataItem itemDataPoint = getSeries(series).getDataItem(item);
    for (XYDataItem key : annotation.keySet()) {
      if (Math.abs(key.getXValue() - itemDataPoint.getXValue()) < 0.0001) {
        return annotation.get(key);
      }
    }
    return null;
  }

  public void addIdentity(MZTolerance mzTolerance, AbstractMSMSIdentity ann) {
    if (ann instanceof AbstractMSMSDataPointIdentity) {
      addDPIdentity(mzTolerance, (AbstractMSMSDataPointIdentity) ann);
    }
    // TODO add diff identity
  }

  private void addDPIdentity(MZTolerance mzTolerance, AbstractMSMSDataPointIdentity ann) {
    for (int s = 0; s < getSeriesCount(); s++) {
      XYSeries series = getSeries(s);
      for (int i = 0; i < series.getItemCount(); i++) {
        XYDataItem dp = series.getDataItem(i);
        if (mzTolerance.checkWithinTolerance(dp.getXValue(), ann.getMZ())) {
          addAnnotation(dp, ann.getName());
        }
      }
    }
  }

  @Override
  public Number getX(int series, int item) {
    return getSeries(series).getX(item);
  }

  @Override
  public Number getY(int series, int item) {
    return getSeries(series).getY(item);
  }

  @Override
  public Number getEndX(int series, int item) {
    return getX(series, item);
  }

  @Override
  public double getEndXValue(int series, int item) {
    return getXValue(series, item);
  }

  @Override
  public Number getEndY(int series, int item) {
    return getY(series, item);
  }

  @Override
  public double getEndYValue(int series, int item) {
    return getYValue(series, item);
  }

  @Override
  public Number getStartX(int series, int item) {
    return getX(series, item);
  }

  @Override
  public double getStartXValue(int series, int item) {
    return getXValue(series, item);
  }

  @Override
  public Number getStartY(int series, int item) {
    return getY(series, item);
  }

  @Override
  public double getStartYValue(int series, int item) {
    return getYValue(series, item);
  }
}