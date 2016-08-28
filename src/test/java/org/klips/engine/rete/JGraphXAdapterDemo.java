package org.klips.engine.rete;
/* This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */

import com.mxgraph.layout.*;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.*;

import java.awt.*;

import javax.swing.*;

import org.klips.engine.rule.AdvancedRuleTest;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.*;
import org.jgrapht.ext.*;
import org.jgrapht.graph.*;


/**
 * A demo applet that shows how to use JGraphX to visualize JGraphT graphs.
 * Applet based on JGraphAdapterDemo.
 *
 * @since July 9, 2013
 */
public class JGraphXAdapterDemo
        extends JApplet
{
    private static final long serialVersionUID = 2202072534703043194L;
    private static final Dimension DEFAULT_SIZE = new Dimension(530, 320);

    private JGraphXAdapter<String, DefaultEdge> jgxAdapter;

    /**
     * An alternative starting point for this demo, to also allow running this
     * applet as an application.
     *
     * @param args ignored.
     */
    public static void main(String [] args)
    {
        JGraphXAdapterDemo applet = new JGraphXAdapterDemo();
        applet.init();

        JFrame frame = new JFrame();
        frame.getContentPane().add(applet);
        frame.setTitle("JGraphT Adapter to JGraph Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * {@inheritDoc}
     */
    public void init()
    {
        // create a JGraphT graph
        ListenableGraph<Node, MyEdge<Node, Node, String>> g = createGraph();

        // create a visualization using JGraph, via an adapter
        jgxAdapter = new JGraphXAdapter(g);


        getContentPane().add(new mxGraphComponent(jgxAdapter));
        resize(DEFAULT_SIZE);

        // positioning via jgraphx layouts

        mxGraphLayout layout = new mxHierarchicalLayout(jgxAdapter);
        layout.execute(jgxAdapter.getDefaultParent());

        // that's all there is to it!...
    }

    @NotNull
    private ListenableGraph<Node, MyEdge<Node, Node, String>> createGraph() {
        ListenableGraph<Node, MyEdge<Node, Node, String>> g =
                new ListenableDirectedGraph<Node, MyEdge<Node, Node, String>>(
                        //new ReteTestGraphBuilder().create()
                        ReteGraphBuilder.INSTANCE.create(
                                new AdvancedRuleTest().createReteBuilder()
                        )
                );

        return g;
    }

    @NotNull
    private ListenableGraph<String, DefaultEdge> createGraph1() {
        ListenableGraph<String, DefaultEdge> g =
                new ListenableUndirectedGraph<String, DefaultEdge>(
                        DefaultEdge.class);


        String v1 = "v1";
        String v2 = "v2";
        String v3 = "v3";
        String v4 = "v4";

        // add some sample data (graph manipulated via JGraphX)
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);

        g.addEdge(v1, v2);
        g.addEdge(v2, v3);
        g.addEdge(v3, v1);
        g.addEdge(v4, v3);
        return g;
    }
}

//End JGraphXAdapterDemo.java