package de.uni_due.inf.ti.graph;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
    GraphFactoryTest.class,
    IsomorphismHashSetTest.class,
    IsomorphismHashMapTest.class,
    MorphismTest.class,
    MorphismTestParam.class,
    MorphismTestRandom.class,
    RuleTestParam.class,
    MorphismEnumeratorTest.class })
public class AllTests
{

}
