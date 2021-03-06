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
package org.sonar.server.startup;

import org.junit.Test;
import org.sonar.api.rules.*;
import org.sonar.jpa.test.AbstractDbUnitTestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.number.OrderingComparisons.greaterThan;
import static org.junit.Assert.*;

public class RegisterRulesTest extends AbstractDbUnitTestCase {

  @Test
  public void saveNewRepositories() {
    setupData("shared");
    RegisterRules task = new RegisterRules(getSessionFactory(), new RuleRepository[]{new FakeRepository()});
    task.start();

    List<Rule> result = getSession().getResults(Rule.class, "pluginName", "fake");
    assertThat(result.size(), is(2));

    Rule first = result.get(0);
    assertThat(first.getKey(), is("rule1"));
    assertThat(first.getRepositoryKey(), is("fake"));
    assertThat(first.isEnabled(), is(true));
    assertThat(first.getParams().size(), is(2));
  }

  @Test
  public void disableDeprecatedRepositories() {
    setupData("shared");
    RegisterRules task = new RegisterRules(getSessionFactory(), new RuleRepository[]{new FakeRepository()});
    task.start();

    List<Rule> rules = (List<Rule>) getSession()
      .createQuery("from " + Rule.class.getSimpleName() + " where pluginName<>'fake'")
      .getResultList();
    assertThat(rules.size(), greaterThan(0));
    for (Rule rule : rules) {
      assertThat(rule.isEnabled(), is(false));
    }
  }

  @Test
  public void disableDeprecatedActiveRules() {
    setupData("disableDeprecatedActiveRules");
    RegisterRules task = new RegisterRules(getSessionFactory(), new RuleRepository[]{new FakeRepository()});
    task.start();

    List<Rule> result = getSession().getResults(Rule.class, "pluginName", "fake");
    assertThat(result.size(), is(3));

    Rule deprecated = result.get(0);
    assertThat(deprecated.getKey(), is("deprecated"));
    assertThat(deprecated.isEnabled(), is(false));

    assertThat(result.get(1).isEnabled(), is(true));
    assertThat(result.get(2).isEnabled(), is(true));
  }

  @Test
  public void disableDeprecatedActiveRuleParameters() {
    setupData("disableDeprecatedActiveRuleParameters");
    RegisterRules task = new RegisterRules(getSessionFactory(), new RuleRepository[]{new FakeRepository()});
    task.start();

    ActiveRule arule = getSession().getSingleResult(ActiveRule.class, "id", 1);
    assertThat(arule.getActiveRuleParams().size(), is(2));
    assertNull(getSession().getSingleResult(ActiveRuleParam.class, "id", 3));
  }

  @Test
  public void disableDeprecatedRules() {
    setupData("disableDeprecatedRules");
    RegisterRules task = new RegisterRules(getSessionFactory(), new RuleRepository[]{new FakeRepository()});
    task.start();

    Rule rule = getSession().getSingleResult(Rule.class, "id", 1);
    assertThat(rule.isEnabled(), is(false));

    rule = getSession().getSingleResult(Rule.class, "id", 2);
    assertThat(rule.isEnabled(), is(false));
  }

  @Test
  public void updateRuleFields() {
    setupData("updadeRuleFields");
    RegisterRules task = new RegisterRules(getSessionFactory(), new RuleRepository[]{new FakeRepository()});
    task.start();

    // fields have been updated with new values
    Rule rule = getSession().getSingleResult(Rule.class, "id", 1);
    assertThat(rule.getName(), is("One"));
    assertThat(rule.getDescription(), is("Description of One"));
    assertThat(rule.getSeverity(), is(RulePriority.BLOCKER));
    assertThat(rule.getConfigKey(), is("config1"));
  }

  @Test
  public void updateRuleParameters() {
    setupData("updateRuleParameters");
    RegisterRules task = new RegisterRules(getSessionFactory(), new RuleRepository[]{new FakeRepository()});
    task.start();

    Rule rule = getSession().getSingleResult(Rule.class, "id", 1);
    assertThat(rule.getParams().size(), is(2));

    // new parameter
    assertNotNull(rule.getParam("param2"));
    assertThat(rule.getParam("param2").getDescription(), is("parameter two"));
    assertThat(rule.getParam("param2").getDefaultValue(), is("default value two"));

    // updated parameter
    assertNotNull(rule.getParam("param1"));
    assertThat(rule.getParam("param1").getDescription(), is("parameter one"));
    assertThat(rule.getParam("param1").getDefaultValue(), is("default value one"));

    // deleted parameter
    assertNull(rule.getParam("deprecated_param"));
    assertNull(getSession().getSingleResult(RuleParam.class, "id", 2)); // id of deprecated_param is 2
  }

  @Test
  public void doNotDisableUserRulesIfParentIsEnabled() {
    setupData("doNotDisableUserRulesIfParentIsEnabled");
    RegisterRules task = new RegisterRules(getSessionFactory(), new RuleRepository[]{new FakeRepository()});
    task.start();

    Rule rule = getSession().getSingleResult(Rule.class, "id", 2);
    assertThat(rule.isEnabled(), is(true));
  }

  @Test
  public void disableUserRulesIfParentIsDisabled() {
    setupData("disableUserRulesIfParentIsDisabled");
    RegisterRules task = new RegisterRules(getSessionFactory(), new RuleRepository[]{new FakeRepository()});
    task.start();

    Rule rule = getSession().getSingleResult(Rule.class, "id", 2);
    assertThat(rule.isEnabled(), is(false));
  }

  @Test
  public void shouldNotDisableManualRules() {
    // the hardcoded repository "manual" is used for manual violations
    setupData("shouldNotDisableManualRules");

    RegisterRules task = new RegisterRules(getSessionFactory(), new RuleRepository[]{new FakeRepository()});
    task.start();

    assertThat(getSession().getSingleResult(Rule.class, "id", 1).isEnabled(), is(true));
    assertThat(getSession().getSingleResult(Rule.class, "id", 2).isEnabled(), is(false));
  }

  @Test
  public void volumeTesting() {
    setupData("shared");
    RegisterRules task = new RegisterRules(getSessionFactory(), new RuleRepository[]{new VolumeRepository()});
    task.start();

    List<Rule> result = getSession().getResults(Rule.class, "enabled", true);
    assertThat(result.size(), is(VolumeRepository.SIZE));
  }
}

class FakeRepository extends RuleRepository {
  public FakeRepository() {
    super("fake", "java");
  }

  public List<Rule> createRules() {
    Rule rule1 = Rule.create("fake", "rule1", "One");
    rule1.setDescription("Description of One");
    rule1.setSeverity(RulePriority.BLOCKER);
    rule1.setConfigKey("config1");
    rule1.createParameter("param1").setDescription("parameter one").setDefaultValue("default value one");
    rule1.createParameter("param2").setDescription("parameter two").setDefaultValue("default value two");

    Rule rule2 = Rule.create("fake", "rule2", "Two");
    rule2.setSeverity(RulePriority.INFO);

    return Arrays.asList(rule1, rule2);
  }
}

class VolumeRepository extends RuleRepository {
  static final int SIZE = 500;

  public VolumeRepository() {
    super("volume", "java");
  }

  public List<Rule> createRules() {
    List<Rule> rules = new ArrayList<Rule>();
    for (int i = 0; i < SIZE; i++) {
      Rule rule = Rule.create("volume", "rule" + i, "description of " + i);
      rule.setSeverity(RulePriority.BLOCKER);
      for (int j = 0; j < 20; j++) {
        rule.createParameter("param" + j);
      }
      rules.add(rule);
    }
    return rules;
  }
}
