/*
 * *************************************************************************************
 *  Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 *  http://esper.codehaus.org                                                          *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 * *************************************************************************************
 */

package com.espertech.esper.view.stat;

import com.espertech.esper.client.EventType;
import com.espertech.esper.support.bean.SupportMarketDataBean;
import com.espertech.esper.support.epl.SupportExprNodeFactory;
import com.espertech.esper.support.event.SupportEventTypeFactory;
import com.espertech.esper.support.view.SupportStatementContextFactory;
import com.espertech.esper.view.TestViewSupport;
import com.espertech.esper.view.ViewFactoryContext;
import com.espertech.esper.view.ViewFieldEnum;
import com.espertech.esper.view.ViewParameterException;
import com.espertech.esper.view.std.FirstElementView;
import junit.framework.TestCase;

public class TestWeightedAverageViewFactory extends TestCase
{
    private WeightedAverageViewFactory factory;
    private ViewFactoryContext viewFactoryContext = new ViewFactoryContext(null, 1, 1, null, null);

    public void setUp()
    {
        factory = new WeightedAverageViewFactory();
    }

    public void testSetParameters() throws Exception
    {
        tryParameter(new Object[] {"price", "volume"}, "price", "volume");

        tryInvalidParameter(new Object[] {"symbol", 1.1d});
        tryInvalidParameter(new Object[] {1.1d, "feed"});
        tryInvalidParameter(new Object[] {1.1d});
        tryInvalidParameter(new Object[] {"feed", "symbol", "feed"});
        tryInvalidParameter(new Object[] {new String[] {"volume", "price"}});
    }

    public void testAttaches() throws Exception
    {
        // Should attach to anything as long as the fields exists
        EventType parentType = SupportEventTypeFactory.createBeanType(SupportMarketDataBean.class);

        factory.setViewParameters(viewFactoryContext, TestViewSupport.toExprListMD(new Object[] {"price", "volume"}));
        factory.attach(parentType, SupportStatementContextFactory.makeContext(), null, null);
        assertEquals(Double.class, factory.getEventType().getPropertyType(ViewFieldEnum.WEIGHTED_AVERAGE__AVERAGE.getName()));

        try
        {
            factory.setViewParameters(viewFactoryContext, TestViewSupport.toExprListMD(new Object[] {"symbol", "feed"}));
            factory.attach(parentType, SupportStatementContextFactory.makeContext(), null, null);
            fail();
        }
        catch (ViewParameterException ex)
        {
            // expected;
        }
    }

    public void testCanReuse() throws Exception
    {
        factory.setViewParameters(viewFactoryContext, TestViewSupport.toExprListMD(new Object[] {"price", "volume"}));
        factory.attach(SupportEventTypeFactory.createBeanType(SupportMarketDataBean.class), SupportStatementContextFactory.makeContext(), null, null);
        assertFalse(factory.canReuse(new FirstElementView()));
        EventType type = WeightedAverageView.createEventType(SupportStatementContextFactory.makeContext(), null, 1);
        assertFalse(factory.canReuse(new WeightedAverageView(SupportStatementContextFactory.makeAgentInstanceContext(), SupportExprNodeFactory.makeIdentNodeMD("price"), SupportExprNodeFactory.makeIdentNodeMD("price"), type, null)));
        assertFalse(factory.canReuse(new WeightedAverageView(SupportStatementContextFactory.makeAgentInstanceContext(), SupportExprNodeFactory.makeIdentNodeMD("price"), SupportExprNodeFactory.makeIdentNodeMD("symbol"), type, null)));
        assertTrue(factory.canReuse(new WeightedAverageView(SupportStatementContextFactory.makeAgentInstanceContext(), SupportExprNodeFactory.makeIdentNodeMD("price"), SupportExprNodeFactory.makeIdentNodeMD("volume"), type, null)));
    }

    private void tryInvalidParameter(Object[] parameters) throws Exception
    {
        try
        {
            factory.setViewParameters(viewFactoryContext, TestViewSupport.toExprListMD(parameters));
            factory.attach(SupportEventTypeFactory.createBeanType(SupportMarketDataBean.class), SupportStatementContextFactory.makeContext(), null, null);
            fail();
        }
        catch (ViewParameterException ex)
        {
            // expected
        }
    }

    private void tryParameter(Object[] parameters, String fieldNameX, String fieldNameW) throws Exception
    {
        factory.setViewParameters(viewFactoryContext, TestViewSupport.toExprListMD(parameters));
        factory.attach(SupportEventTypeFactory.createBeanType(SupportMarketDataBean.class), SupportStatementContextFactory.makeContext(), null, null);
        WeightedAverageView view = (WeightedAverageView) factory.makeView(SupportStatementContextFactory.makeAgentInstanceViewFactoryContext());
        assertEquals(fieldNameX, view.getFieldNameX().toExpressionString());
        assertEquals(fieldNameW, view.getFieldNameWeight().toExpressionString());
    }
}
