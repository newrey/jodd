// Copyright (c) 2003-present, Jodd Team (jodd.org). All Rights Reserved.

package jodd.lagarto.dom;

import jodd.csselly.CSSelly;
import jodd.csselly.Combinator;
import jodd.csselly.CssSelector;
import jodd.util.collection.JoddArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Node selector selects DOM nodes using {@link CSSelly CSS3 selectors}.
 * Group of queries are supported.
 */
public class NodeSelector {

	protected final Node rootNode;

	public NodeSelector(Node rootNode) {
		this.rootNode = rootNode;
	}

	// ---------------------------------------------------------------- selector

	/**
	 * Selects nodes using CSS3 selector query.
	 */
	public List<Node> select(String query) {
		Collection<List<CssSelector>> selectorsCollection = CSSelly.parse(query);
		return select(selectorsCollection);
	}

	/**
	 * Selected nodes using pre-parsed CSS selectors. Take in consideration
	 * collection type for results grouping order.
	 */
	public List<Node> select(Collection<List<CssSelector>> selectorsCollection) {
		List<Node> results = new ArrayList<Node>();
		for (List<CssSelector> selectors : selectorsCollection) {
			processSelectors(results, selectors);
		}
		return results;
	}

	/**
	 * Process selectors and keep adding results.
	 */
	protected void processSelectors(List<Node> results, List<CssSelector> selectors) {
		List<Node> selectedNodes = select(rootNode, selectors);

		for (Node selectedNode : selectedNodes) {
			if (!results.contains(selectedNode)) {
				results.add(selectedNode);
			}
		}
	}

	/**
	 * Selects nodes using CSS3 selector query and returns the very first one.
	 */
	public Node selectFirst(String query) {
		List<Node> selectedNodes = select(query);
		if (selectedNodes.isEmpty()) {
			return null;
		}
		return selectedNodes.get(0);
	}

	/**
	 * Selects nodes using {@link NodeFilter node filter}.
	 */
	public List<Node> select(NodeFilter nodeFilter) {
		List<Node> nodes = new ArrayList<Node>();
		walk(rootNode, nodeFilter, nodes);
		return nodes;
	}

	/**
	 * Selects nodes using {@link NodeFilter node filter} and return the very first one.
	 */
	public Node selectFirst(NodeFilter nodeFilter) {
		List<Node> selectedNodes = select(nodeFilter);
		if (selectedNodes.isEmpty()) {
			return null;
		}
		return selectedNodes.get(0);
	}

	// ---------------------------------------------------------------- internal

	protected void walk(Node rootNode, NodeFilter nodeFilter, List<Node> result) {
		int childCount = rootNode.getChildNodesCount();
		for (int i = 0; i < childCount; i++) {
			Node node = rootNode.getChild(i);
			if (nodeFilter.accept(node)) {
				result.add(node);
			}
			walk(node, nodeFilter, result);
		}
	}

	protected List<Node> select(Node rootNode, List<CssSelector> selectors) {

		// start with the root node
		List<Node> nodes = new ArrayList<Node>();
		nodes.add(rootNode);

		// iterate all selectors
		for (CssSelector cssSelector : selectors) {

			// create new set of results for current css selector
			List<Node> selectedNodes = new ArrayList<Node>();
			for (Node node : nodes) {
				walk(node, cssSelector, selectedNodes);
			}

			// post-processing: filter out the results
			List<Node> resultNodes = new ArrayList<Node>();
			int index = 0;
			for (Node node : selectedNodes) {
				boolean match = filter(selectedNodes, node, cssSelector, index);
				if (match == true) {
					resultNodes.add(node);
				}
				index++;
			}

			// continue with results
			nodes = resultNodes;
		}

		return nodes;
	}

	/**
	 * Walks over the child notes, maintaining the tree order and not using recursion.
	 */
	protected void walkDescendantsIteratively(JoddArrayList<Node> nodes, CssSelector cssSelector, List<Node> result) {
		while (!nodes.isEmpty()) {
			Node node = nodes.removeFirst();
			selectAndAdd(node, cssSelector, result);

			// append children in walking order to be processed right after this node
			int childCount = node.getChildNodesCount();
			for (int i = childCount - 1; i >= 0; i--) {
				nodes.addFirst(node.getChild(i));
			}
		}
	}

	/**
	 * Finds nodes in the tree that matches single selector.
	 */
	protected void walk(Node rootNode, CssSelector cssSelector, List<Node> result) {

		// previous combinator determines the behavior
		CssSelector previousCssSelector = cssSelector.getPrevCssSelector();

		Combinator combinator = previousCssSelector != null ?
				previousCssSelector.getCombinator() :
				Combinator.DESCENDANT;

		switch (combinator) {
			case DESCENDANT:
				JoddArrayList<Node> nodes = new JoddArrayList<Node>();
				int childCount = rootNode.getChildNodesCount();
				for (int i = 0; i < childCount; i++) {
					nodes.add(rootNode.getChild(i));
					// recursive
//					selectAndAdd(node, cssSelector, result);
//					walk(node, cssSelector, result);
				}
				walkDescendantsIteratively(nodes, cssSelector, result);
				break;
			case CHILD:
				childCount = rootNode.getChildNodesCount();
				for (int i = 0; i < childCount; i++) {
					Node node = rootNode.getChild(i);
					selectAndAdd(node, cssSelector, result);
				}
				break;
			case ADJACENT_SIBLING:
				Node node = rootNode.getNextSiblingElement();
				if (node != null) {
					selectAndAdd(node, cssSelector, result);
				}
				break;
			case GENERAL_SIBLING:
				node = rootNode;
				while (true) {
					node = node.getNextSiblingElement();
					if (node == null) {
						break;
					}
					selectAndAdd(node, cssSelector, result);
				}
				break;
		}	}

	/**
	 * Selects single node for single selector and appends it to the results.
	 */
	protected void selectAndAdd(Node node, CssSelector cssSelector, List<Node> result) {
		// ignore all nodes that are not elements
		if (node.getNodeType() != Node.NodeType.ELEMENT) {
			return;
		}
		boolean matched = cssSelector.accept(node);
		if (matched) {
			// check for duplicates
			if (result.contains(node)) {
				return;
			}
			// no duplicate found, add it to the results
			result.add(node);
		}
	}

	/**
	 * Filter nodes.
	 */
	protected boolean filter(List<Node> currentResults, Node node, CssSelector cssSelector, int index) {
		return cssSelector.accept(currentResults, node, index);
	}

}