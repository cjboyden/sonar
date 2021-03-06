/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2012 SonarSource
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
package org.sonar.plugins.dbcleaner.purges;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.time.DateUtils;
import org.sonar.api.database.DatabaseSession;
import org.sonar.api.database.model.Snapshot;
import org.sonar.api.utils.Logs;
import org.sonar.plugins.dbcleaner.api.Purge;
import org.sonar.plugins.dbcleaner.api.PurgeContext;
import org.sonar.plugins.dbcleaner.api.PurgeUtils;

import javax.persistence.Query;
import java.util.Date;
import java.util.List;

/**
 * @since 1.11
 */
public final class PurgeUnprocessed extends Purge {

  private Configuration configuration;

  public PurgeUnprocessed(DatabaseSession session, Configuration conf) {
    super(session);
    this.configuration = conf;
  }

  public void purge(PurgeContext context) {
    int minimumPeriodInHours = PurgeUtils.getMinimumPeriodInHours(configuration);
    final Date beforeDate = DateUtils.addHours(new Date(), -minimumPeriodInHours);
    Logs.INFO.info("Deleting unprocessed data before " + beforeDate);

    Query query = getSession().createQuery("SELECT s.id FROM " + Snapshot.class.getSimpleName() + " s WHERE s.last=:last AND status=:status AND s.createdAt<:date");
    query.setParameter("status", Snapshot.STATUS_UNPROCESSED);
    query.setParameter("date", beforeDate);
    query.setParameter("last", Boolean.FALSE);
    List<Integer> snapshotIds = query.getResultList();

    PurgeUtils.deleteSnapshotsData(getSession(), snapshotIds);
  }
}
