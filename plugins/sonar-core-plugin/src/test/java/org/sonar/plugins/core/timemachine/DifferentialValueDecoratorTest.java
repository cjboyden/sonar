/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.core.timemachine;

import org.junit.Test;
import org.sonar.api.database.model.MeasureModel;
import org.sonar.api.database.model.Snapshot;
import org.sonar.api.measures.Metric;
import org.sonar.jpa.test.AbstractDbUnitTestCase;

import java.util.List;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DifferentialValueDecoratorTest extends AbstractDbUnitTestCase {

  private static final int PROJECT_SNAPSHOT_ID = 1000;
  private static final int PROJECT_ID = 1;
  private static final int FILE_ID = 3;

  @Test
  public void shouldSelectPastResourceMeasures() {
    setupData("shared");

    Metric[] metrics = selectMetrics();
    Snapshot projectSnapshot = getSession().getSingleResult(Snapshot.class, "id", PROJECT_SNAPSHOT_ID);

    DifferentialValueDecorator decorator = new DifferentialValueDecorator(getSession(), new Snapshot[0], metrics);
    List<MeasureModel> measures = decorator.selectPastMeasures(FILE_ID, projectSnapshot);
    assertThat(measures.size(), is(2));

    for (MeasureModel measure : measures) {
      assertThat(measure.getId(), anyOf(is(5L), is(6L)));
      assertThat(measure.getValue(), anyOf(is(5.0), is(60.0)));
    }
  }

  @Test
  public void shouldSelectPastProjectMeasures() {
    setupData("shared");

    Metric[] metrics = selectMetrics();
    Snapshot projectSnapshot = getSession().getSingleResult(Snapshot.class, "id", PROJECT_SNAPSHOT_ID);

    DifferentialValueDecorator decorator = new DifferentialValueDecorator(getSession(), new Snapshot[0], metrics);
    List<MeasureModel> measures = decorator.selectPastMeasures(PROJECT_ID, projectSnapshot);
    assertThat(measures.size(), is(2));

    for (MeasureModel measure : measures) {
      assertThat(measure.getId(), anyOf(is(1L), is(2L)));
      assertThat(measure.getValue(), anyOf(is(60.0), is(80.0)));
    }
  }

  private Metric[] selectMetrics() {
    List<Metric> metrics = getSession().getResults(Metric.class);
    return metrics.toArray(new Metric[metrics.size()]);
  }
}