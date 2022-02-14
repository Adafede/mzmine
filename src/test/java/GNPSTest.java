/*
 * Copyright 2006-2021 The MZmine Development Team
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

import io.github.mzmine.datamodel.DataPoint;
import io.github.mzmine.datamodel.impl.SimpleDataPoint;
import io.github.mzmine.modules.io.export_features_gnps.GNPSUtils;
import io.github.mzmine.modules.io.export_features_gnps.masst.MasstDatabase;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 * @author Robin Schmid (https://github.com/robinschmid)
 */
public class GNPSTest {

  private static final Logger logger = Logger.getLogger(GNPSTest.class.getName());

  @Test
  void testMasstSearch() throws IOException {
    // GNPS demo data
    String demo = """
        463.381	43.591
        693.498	119.206
        694.496	42.985
        707.494	508.18
        708.512	197.117
        709.558	18.679
        723.4	43.831
        800.494	476.556
        801.518	196.451
        802.496	95.972
        814.513	86.182
        931.574	50.803
        972.868	14.634
        1016.62	66.809
        1017.58	16.578
        1025.57	22.426""";
    DataPoint[] dps = Arrays.stream(demo.split("\n")).map(line -> line.split("	"))
        .map(dp -> new SimpleDataPoint(Double.parseDouble(dp[0]), Double.parseDouble(dp[1])))
        .toArray(DataPoint[]::new);

    String result = GNPSUtils.submitMASSTJob("MZmine3 quickstart", dps, 1044.66, MasstDatabase.ALL,
        0.7, 1d, 0.5, 6, false, "", "", "", true);
    Assert.assertFalse(result.isBlank());
  }
}
