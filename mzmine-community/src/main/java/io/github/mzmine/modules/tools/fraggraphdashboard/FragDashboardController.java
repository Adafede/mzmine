/*
 * Copyright (c) 2004-2024 The MZmine Development Team
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.mzmine.modules.tools.fraggraphdashboard;

import com.google.common.collect.Range;
import io.github.mzmine.datamodel.MassSpectrum;
import io.github.mzmine.datamodel.Scan;
import io.github.mzmine.javafx.mvci.FxController;
import io.github.mzmine.javafx.mvci.FxViewBuilder;
import io.github.mzmine.main.ConfigService;
import io.github.mzmine.modules.dataprocessing.id_formulaprediction.ResultFormula;
import io.github.mzmine.modules.tools.fraggraphdashboard.spectrumplottable.SpectrumPlotTableController;
import io.github.mzmine.modules.tools.fraggraphdashboard.spectrumplottable.SpectrumPlotTableViewBuilder.Layout;
import io.github.mzmine.modules.tools.fraggraphdashboard.fraggraph.graphstream.SubFormulaEdge;
import io.github.mzmine.modules.tools.fraggraphdashboard.fraggraph.mvci.FragmentGraphController;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.util.exceptions.MissingMassListException;
import java.util.List;
import javafx.collections.ListChangeListener;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openscience.cdk.interfaces.IMolecularFormula;

public class FragDashboardController extends FxController<FragDashboardModel> {

  private final FragDashboardBuilder fragDashboardBuilder;
  private final FragmentGraphController fragmentGraphController;

  private final ParameterSet parameters;
  private SpectrumPlotTableController isotopeController;
  private SpectrumPlotTableController ms2Controller;

  public FragDashboardController() {
    this(ConfigService.getConfiguration().getModuleParameters(FragDashboardModule.class));
  }

  public FragDashboardController(@Nullable ParameterSet parameters) {
    super(new FragDashboardModel());
    this.parameters = parameters != null ? parameters
        : ConfigService.getConfiguration().getModuleParameters(FragDashboardModule.class);
    fragmentGraphController = new FragmentGraphController(parameters);

//    model.precursorFormulaProperty()
//        .bindBidirectional(fragmentGraphController.precursorFormulaProperty());
//    model.spectrumProperty().bindBidirectional(fragmentGraphController.spectrumProperty());
    model.allEdgesProperty().bindContentBidirectional(fragmentGraphController.allEdgesProperty());
    model.allNodesProperty().bindContentBidirectional(fragmentGraphController.allNodesProperty());
    model.selectedEdgesProperty()
        .bindContentBidirectional(fragmentGraphController.selectedEdgesProperty());
    model.selectedNodesProperty()
        .bindContentBidirectional(fragmentGraphController.selectedNodesProperty());
    fragmentGraphController.measuredPrecursorMzProperty() // regular binding so we take control of the property with this controller.
        .bind(model.precursorMzProperty().map(Number::doubleValue));

    ms2Controller = new SpectrumPlotTableController(Layout.HORIZONTAL);
    isotopeController = new SpectrumPlotTableController(Layout.HORIZONTAL);

    model.spectrumProperty().bindBidirectional(ms2Controller.spectrumProperty());
    model.isotopePatternProperty().bindBidirectional(isotopeController.spectrumProperty());

    fragDashboardBuilder = new FragDashboardBuilder(model, fragmentGraphController.buildView(),
        ms2Controller.buildView(), isotopeController.buildView(), this::updateFragmentGraph,
        this::startFormulaCalculation, parameters);

    initSelectedEdgeToSpectrumListener();
  }

  @Override
  protected @NotNull FxViewBuilder<FragDashboardModel> getViewBuilder() {
    return fragDashboardBuilder;
  }

  public void updateFragmentGraph() {
    fragmentGraphController.precursorFormulaProperty().set(model.getPrecursorFormula());
    fragmentGraphController.spectrumProperty().set(model.getSpectrum());
  }

  public void startFormulaCalculation() {
    onTaskThreadDelayed(new FragGraphPrecursorFormulaTask(model, parameters), new Duration(200));
  }

  public void setInput(double precursorMz, @NotNull MassSpectrum ms2Spectrum,
      @Nullable MassSpectrum isotopePattern) {
    model.setPrecursorMz(precursorMz);
    model.setSpectrum(ms2Spectrum);
    model.setIsotopePattern(isotopePattern != null ? isotopePattern : MassSpectrum.EMPTY);
  }

  public void setInput(double precursorMz, @NotNull MassSpectrum ms2Spectrum,
      @Nullable MassSpectrum isotopePattern, @Nullable IMolecularFormula formula) {
    if (ms2Spectrum instanceof Scan s) {
      if (s.getMassList() == null) {
        throw new MissingMassListException(s);
      }
      ms2Spectrum = s.getMassList();
    }
    setInput(precursorMz, ms2Spectrum, isotopePattern);
    model.setPrecursorFormula(formula);
  }

  public void setInput(double precursorMz, @NotNull MassSpectrum ms2Spectrum,
      @Nullable MassSpectrum isotopePattern, @Nullable IMolecularFormula formula,
      @Nullable List<ResultFormula> formulae) {
    setInput(precursorMz, ms2Spectrum, isotopePattern, formula);
    if (formulae != null) {
      model.precursorFormulaeProperty().setAll(formulae);
    }
  }

  private void initSelectedEdgeToSpectrumListener() {
    model.selectedEdgesProperty().addListener(new ListChangeListener<SubFormulaEdge>() {
      @Override
      public void onChanged(Change<? extends SubFormulaEdge> change) {
        while (change.next()) {
          if (change.wasAdded()) {
            for (SubFormulaEdge edge : change.getAddedSubList()) {
              ms2Controller.addDomainMarker(
                  Range.closed(edge.smaller().getPeakWithFormulae().peak().getMZ(),
                      edge.larger().getPeakWithFormulae().peak().getMZ()));
            }
          }
          if (change.wasRemoved()) {
            for (SubFormulaEdge edge : change.getRemoved()) {
              ms2Controller.removeDomainMarker(
                  Range.closed(edge.smaller().getPeakWithFormulae().peak().getMZ(),
                      edge.larger().getPeakWithFormulae().peak().getMZ()));
            }
          }
        }
      }
    });
  }
}
