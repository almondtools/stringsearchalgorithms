package com.almondtools.util.map;

import static java.util.Arrays.asList;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

/**
 * provides base functionality for a minimal perfect HashMap<T,S> (no hash collisions, no unused space) with the BMZ algorithm 
 */
public class MinimalPerfectMapBuilder<T, S> {

	private KeySerializer<T> keySerializer;
	private S defaultValue;
	private Map<T, S> entries;

	private HashFunction h;
	private int m;
	private int n;

	public MinimalPerfectMapBuilder(S defaultValue) {
		this.defaultValue = defaultValue;
		this.entries = new HashMap<T, S>();
	}
	
	public MinimalPerfectMapBuilder<T, S> withKeySerializer(KeySerializer<T> keySerializer) {
		this.keySerializer = keySerializer;
		return this;
	}
	
	public S get(T key) {
		S result = entries.get(key);
		if (result == null) {
			return defaultValue;
		} else {
			return result;
		}
	}

	public void put(T key, S value) {
		entries.put(key, value);
	}

	public S getDefaultValue() {
		return defaultValue;
	}

	public Map<T, S> getEntries() {
		return entries;
	}

	protected void computeFunctions(int maxTries, double c) throws HashBuildException {
		m = entries.size();
		n = 1 + (int) (m * c);

		Random r = new Random(17);
		for (int i = 0; i < maxTries; i++) {
			try {
				int seed1 = r.nextInt();
				int seed2 = r.nextInt();
				h = computeH(seed1, seed2);
				break;
			} catch (HashBuildException e) {
				continue;
			}
		}
		if (h == null) {
			throw new HashBuildException();
		}
	}

	private HashFunction computeH(int seed1, int seed2) throws HashBuildException {
		int[] g = new int[n];
		HashFunction h = new HashFunction(g, seed1, seed2);
		Graph graph = buildGraph(h);
		BitSet assignedNodes = new BitSet(n);
		BitSet assignedEdges = new BitSet(m);
		assignIntegersToCriticalNodes(graph, g, assignedNodes, assignedEdges);
		assignIntegersToNonCriticalNodes(graph, g, assignedNodes, assignedEdges);
		return h;
	}

	private void assignIntegersToCriticalNodes(Graph graph, int[] g, BitSet assignedNodes, BitSet assignedEdges) throws HashBuildException {
		for (int i : graph.findCriticalNodes()) {
			assignIntegersToCriticalNodes(graph, g, i, assignedNodes, assignedEdges);
		}
	}

	private void assignIntegersToCriticalNodes(Graph graph, int[] g, int i, BitSet assignedNodes, BitSet assignedEdges) throws HashBuildException {
		if (!assignedNodes.get(i)) {
			int next = 0;
			g[i] = next;
			assignedNodes.set(i);
			Queue<Integer> worklist = new LinkedList<Integer>();
			worklist.add(i);
			while (!worklist.isEmpty()) {
				int v = worklist.remove();
				for (int u : graph.adjacents(v)) {
					if (!assignedNodes.get(u)) {
						next = assignNext(next + 1, graph, g, u, assignedNodes, assignedEdges);
						worklist.add(u);
					}
				}
			}

		}
	}

	private int assignNext(int next, Graph graph, int[] g, int u, BitSet assignedNodes, BitSet assignedEdges) throws HashBuildException {
		while (!isAssignable(next, graph, g, u, assignedNodes, assignedEdges)) {
			next++;
		}
		g[u] = next;
		assignedNodes.set(u);
		for (int w : graph.adjacents(u)) {
			if (assignedNodes.get(w)) {
				int edge = next + g[w];
				assignedEdges.set(edge);
			}
		}
		return next;
	}

	private boolean isAssignable(int next, Graph graph, int[] g, int u, BitSet assignedNodes, BitSet assignedEdges) throws HashBuildException {
		for (int w : graph.adjacents(u)) {
			if (assignedNodes.get(w)) {
				int edge = next + g[w];
				if (edge >= m) {
					throw new HashBuildException();
				}
				if (assignedEdges.get(edge)) {
					return false;
				}
			}
		}
		return true;
	}

	private void assignIntegersToNonCriticalNodes(Graph graph, int[] g, BitSet assignedNodes, BitSet assignedEdges) {
		for (int i : graph.findNonCriticalNodes()) {
			assignIntegersToNonCriticalNodes(graph, g, i, assignedNodes, assignedEdges);
		}
	}

	private void assignIntegersToNonCriticalNodes(Graph graph, int[] g, int i, BitSet assignedNodes, BitSet assignedEdges) {
		if (!assignedNodes.get(i)) {
			int next = 0;
			g[i] = next;
			assignedNodes.set(i);
			Queue<Integer> worklist = new LinkedList<Integer>();
			worklist.add(i);
			while (!worklist.isEmpty()) {
				int v = worklist.remove();
				for (int u : graph.adjacents(v)) {
					if (!assignedNodes.get(u)) {
						int edge = assignedEdges.nextClearBit(0);
						g[u] = edge - g[v];
						assignedNodes.set(u);
						assignedEdges.set(edge);
						worklist.add(u);
					}
				}
			}
		}
	}

	private Graph buildGraph(HashFunction h) throws HashBuildException {
		Graph graph = new Graph(n);
		for (T k : entries.keySet()) {
			int[] edge = doubleHash(h, k);
			boolean success = graph.addEdge(edge[0], edge[1]);
			if (!success) {
				throw new HashBuildException();
			}
		}
		return graph;
	}

	private int[] doubleHash(HashFunction h, T k) {
		if (keySerializer == null) {
			return h.doubleHash(k.hashCode());
		} else {
			return h.doubleHash(keySerializer.toLongArray(k));
		}
	}

	protected HashFunction getH() throws HashBuildException {
		return h;
	}

	private static class Graph {

		private int n;
		private List<Set<Integer>> adjacencyList;

		@SuppressWarnings("unchecked")
		public Graph(int n) {
			this.n = n;
			this.adjacencyList = asList((Set<Integer>[]) new Set<?>[n]);
		}

		public boolean addEdge(int a, int b) {
			if (adjacents(a).contains(b) || adjacents(b).contains(a)) {
				return false;
			}
			adjacents(a).add(b);
			adjacents(b).add(a);
			return true;
		}

		public Set<Integer> adjacents(int a) {
			Set<Integer> adjacents = adjacencyList.get(a);
			if (adjacents == null) {
				adjacents = new LinkedHashSet<>();
				adjacencyList.set(a, adjacents);
			}
			return adjacents;
		}

		public Set<Integer> findCriticalNodes() {
			int[] degrees = new int[n];
			for (int a = 0; a < degrees.length; a++) {
				degrees[a] += adjacents(a).size();
			}
			Queue<Integer> todo = new LinkedList<>();
			for (int i = 0; i < degrees.length; i++) {
				if (degrees[i] == 1) {
					todo.add(i);
				}
			}
			while (!todo.isEmpty()) {
				int a = todo.remove();
				for (int b : adjacents(a)) {
					degrees[b]--;
					if (degrees[b] == 1) {
						todo.add(b);
					}
				}
			}
			Set<Integer> result = new HashSet<>();
			for (int i = 0; i < degrees.length; i++) {
				if (degrees[i] > 1) {
					result.add(i);
				}
			}
			return result;
		}

		public Set<Integer> findNonCriticalNodes() {
			Set<Integer> result = new HashSet<>();
			for (int i = 0; i < n; i++) {
				result.add(i);
			}
			result.removeAll(findCriticalNodes());
			return result;
		}

	}

}
