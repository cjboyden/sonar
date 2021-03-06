#
# Sonar, open source software quality management tool.
# Copyright (C) 2008-2012 SonarSource
# mailto:contact AT sonarsource DOT com
#
# Sonar is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 3 of the License, or (at your option) any later version.
#
# Sonar is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with Sonar; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
#
class Drilldown
  attr_reader :snapshot, :columns, :metric, :resource, :highlighted_resource, :highlighted_snapshot

  def initialize(resource, metric, selected_resource_ids, options={})
    @resource=resource
    @snapshot=Snapshot.find(:first, :conditions => {:islast => true, :project_id => resource.id}, :include => [:project])
    @metric=metric
    @columns=[]

    column=nil
    for index in (Project::SCOPES.index(@snapshot.scope)...Project::SCOPES.size)
      snapshot = (column ? column.next_snapshot : @snapshot)
      column=DrilldownColumn.new(snapshot, metric, Project::SCOPES[index], selected_resource_ids, options)
      @columns<<column if column.display?
      if column.selected_snapshot
        @highlighted_snapshot=column.selected_snapshot
        @highlighted_resource=column.selected_snapshot.project         
      end
    end
  end

  def display_value?
    ProjectMeasure.exists?(["snapshot_id=? and metric_id=? and value is not null", @snapshot.id, @metric.id])
  end

  def display_period?(period_index)
    ProjectMeasure.exists?(["snapshot_id=? and metric_id=? and variation_value_#{period_index.to_i} is not null", @snapshot.id, @metric.id])
  end
end

class DrilldownColumn
  attr_reader :measures, :scope, :selected_snapshot, :snapshot, :resource_per_sid

  def initialize(snapshot, metric, scope, selected_resource_ids, options)
    @scope=scope
    @snapshot = snapshot

    value_column = (options[:period] ? "variation_value_#{options[:period]}" : 'value')
    order="project_measures.#{value_column}"
    if metric.direction<0
      order += ' DESC'
    end

    conditions="snapshots.root_snapshot_id=:root_sid AND snapshots.islast=:islast AND snapshots.scope=:scope AND snapshots.path LIKE :path AND project_measures.metric_id=:metric_id AND project_measures.#{value_column} IS NOT NULL"
    condition_values={
      :root_sid => (snapshot.root_snapshot_id || snapshot.id),
      :islast=>true,
      :scope => scope,
      :metric_id=>metric.id,
      :path => "#{snapshot.path}#{snapshot.id}.%"}

    if value_column=='value' && metric.best_value
      conditions<<' AND project_measures.value<>:best_value'
      condition_values[:best_value]=metric.best_value
    end

    if options[:exclude_zero_value]
      conditions += " AND project_measures.#{value_column}<>0"
    end

    if options[:rule_id]
      conditions += ' AND project_measures.rule_id=:rule'
      condition_values[:rule]=options[:rule_id]
    else
      conditions += ' AND project_measures.rule_id IS NULL '
    end

    if options[:characteristic]
      conditions += ' AND project_measures.characteristic_id=:characteristic_id'
      condition_values[:characteristic_id]=options[:characteristic].id
    else
      conditions += ' AND project_measures.characteristic_id IS NULL'
    end

    @measures=ProjectMeasure.find(:all,
      :select => "project_measures.id,project_measures.metric_id,project_measures.#{value_column},project_measures.text_value,project_measures.alert_status,project_measures.alert_text,project_measures.snapshot_id",
      :joins => :snapshot,
      :conditions => [conditions, condition_values],
      :order => order,
      :limit => 200)

    @resource_per_sid={}
    sids=@measures.map{|m| m.snapshot_id}.compact.uniq
    unless sids.empty?
      Snapshot.find(:all,
        :include => :project,
        :conditions => {'snapshots.id' => sids}).each do |snapshot|
          @resource_per_sid[snapshot.id]=snapshot.project
          if selected_resource_ids.include?(snapshot.project_id)
            @selected_snapshot=snapshot
          end
      end
    end
  end

  def resource(measure)
    @resource_per_sid[measure.snapshot_id]
  end

  def display?
    @measures.size>0
  end

  def selected_snapshot
    @selected_snapshot
  end

  def next_snapshot
    @selected_snapshot || @snapshot
  end
end