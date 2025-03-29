package de.uni_due.inf.ti.graph;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
public class RuleTestParam
{
    private String name;
    private Rule rule;
    private Morphism match;
    private Graph result;

    public RuleTestParam(String n, Rule r, Morphism m, Graph res)
    {
        name = n;
        rule = r;
        match = m;
        result = res;
    }
    
    
    @Parameters(name="{0}")
    public static Collection<Object[]> createParams()
    {
        TestGraphs tg = new TestGraphs();
        
        Object[][] params = {
            { "A->B on TriA", tg.rule_A_B, tg.match_A_TriA, tg.graphTriAAB },
            { "A->AA(1) on AA", tg.rule_A_AA, tg.match_A_AA_1, tg.graphAArA },
            { "A->AA(2) on AA", tg.rule_A_AA, tg.match_A_AA_2, tg.graphAAA },
            { "A->AA on AB", tg.rule_A_AB, tg.match_A_AB, tg.graphABrB }
        };
        return Arrays.asList(params);
    }
    
    
    @Test
    public void testApplyToMatch()
    {
        Morphism m = rule.applyToMatch(match).getMorphism();
        Morphism iso = Morphism.getIsomorphism(m.getCodomain(), result);
        assertNotNull("applyToMatch: " + name, iso);
    }

}
