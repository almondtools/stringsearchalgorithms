package com.almondtools.stringsandchars.patternsearch;

import static com.almondtools.stringsandchars.patternsearch.GlushkovAnalyzerOption.FACTORS;
import static com.almondtools.stringsandchars.patternsearch.GlushkovAnalyzerOption.SELF_LOOP;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.almondtools.stringsandchars.regex.AlternativesNode;
import com.almondtools.stringsandchars.regex.AnyCharNode;
import com.almondtools.stringsandchars.regex.BoundedLoopNode;
import com.almondtools.stringsandchars.regex.CharClassNode;
import com.almondtools.stringsandchars.regex.CompClassNode;
import com.almondtools.stringsandchars.regex.ConcatNode;
import com.almondtools.stringsandchars.regex.DefinedCharNode;
import com.almondtools.stringsandchars.regex.EmptyNode;
import com.almondtools.stringsandchars.regex.GroupNode;
import com.almondtools.stringsandchars.regex.OptionalNode;
import com.almondtools.stringsandchars.regex.RangeCharNode;
import com.almondtools.stringsandchars.regex.RegexNode;
import com.almondtools.stringsandchars.regex.RegexNodeVisitor;
import com.almondtools.stringsandchars.regex.SingleCharNode;
import com.almondtools.stringsandchars.regex.SpecialCharClassNode;
import com.almondtools.stringsandchars.regex.StringNode;
import com.almondtools.stringsandchars.regex.UnboundedLoopNode;
import com.almondtools.util.map.BitSetObjectMap;
import com.almondtools.util.map.CharObjectMap;
import com.almondtools.util.map.MinimalPerfectMapBuilder;

public class GlushkovAnalyzer implements RegexNodeVisitor<Void> {

	private RegexNode root;
	private List<DefinedCharNode> charCollector;
	private Map<RegexNode, Set<Integer>> first;
	private Map<RegexNode, Set<Integer>> last;
	private Map<Integer, Set<Integer>> follow;
	private Map<Integer, Set<Integer>> precede;
	private Map<RegexNode, Boolean> empty;
	private DefinedCharNode[] chars;
	private int len;
	private char[] alphabet;

	public GlushkovAnalyzer(RegexNode root) {
		this.root = root;
		this.first = new LinkedHashMap<>();
		this.last = new LinkedHashMap<>();
		this.follow = new LinkedHashMap<>();
		this.precede = new LinkedHashMap<>();
		this.empty = new LinkedHashMap<>();
		this.charCollector = new ArrayList<>();
		this.charCollector.add(null);
	}

	private DefinedCharNode[] characters() {
		return charCollector.toArray(new DefinedCharNode[0]);
	}

	private char[] alphabet() {
		Set<Character> distinctChars = new LinkedHashSet<>();
		for (DefinedCharNode node : charCollector) {
			if (node == null) {
				continue;
			}
			for (char c : node.chars()) {
				distinctChars.add(c);
			}
		}
		char[] alphabet = new char[distinctChars.size()];
		int i = 0;
		for (Character c : distinctChars) {
			alphabet[i] = c;
			i++;
		}
		return alphabet;
	}

	private void first(RegexNode node, Integer... value) {
		first(node, new LinkedHashSet<>(asList(value)));
	}

	private void first(RegexNode node, Set<Integer> value) {
		first.put(node, value);
	}

	private Set<Integer> first(RegexNode node) {
		return first.get(node);
	}

	private List<Set<Integer>> first(List<RegexNode> nodes) {
		List<Set<Integer>> result = new ArrayList<>(nodes.size());
		for (RegexNode node : nodes) {
			result.add(first(node));
		}
		return result;
	}

	private void last(RegexNode node, Integer... value) {
		last(node, new LinkedHashSet<>(asList(value)));
	}

	private void last(RegexNode node, Set<Integer> value) {
		last.put(node, value);
	}

	private Set<Integer> last(RegexNode node) {
		return last.get(node);
	}

	private List<Set<Integer>> last(List<RegexNode> nodes) {
		List<Set<Integer>> result = new ArrayList<>(nodes.size());
		for (RegexNode node : nodes) {
			result.add(last(node));
		}
		return result;
	}

	private void empty(RegexNode node, Boolean value) {
		empty.put(node, value);
	}

	private Boolean empty(RegexNode node) {
		return empty.get(node);
	}

	private List<Boolean> empty(List<RegexNode> nodes) {
		List<Boolean> result = new ArrayList<>(nodes.size());
		for (RegexNode node : nodes) {
			result.add(empty(node));
		}
		return result;
	}

	private void appendFollow(int key, Collection<Integer> append) {
		Set<Integer> followSet = follow.get(key);
		if (followSet == null) {
			followSet = new LinkedHashSet<Integer>();
			follow.put(key, followSet);
		}
		followSet.addAll(append);
	}

	private Set<Integer> follow(Integer i) {
		Set<Integer> set = follow.get(i);
		if (set == null) {
			return Collections.emptySet();
		} else {
			return set;
		}
	}

	private void appendPrecede(int key, Collection<Integer> append) {
		Set<Integer> precedeSet = precede.get(key);
		if (precedeSet == null) {
			precedeSet = new LinkedHashSet<Integer>();
			precede.put(key, precedeSet);
		}
		precedeSet.addAll(append);
	}

	private Set<Integer> precede(Integer i) {
		Set<Integer> set = precede.get(i);
		if (set == null) {
			return Collections.emptySet();
		} else {
			return set;
		}
	}

	public GlushkovAnalyzer analyze() {
		root.accept(this);
		appendFollow(0, first(root));
		for (int f : first(root)) {
			appendPrecede(f, asList(0));
		}
		chars = characters();
		len = chars.length;
		alphabet = alphabet();
		return this;
	}

	public GlushkovAutomaton buildAutomaton(GlushkovAnalyzerOption... options) {
		BitSet initial = FACTORS.in(options) ? all() : initial();

		BitSet finals = finals();

		CharObjectMap<BitSet> reachableByChar = reachableByChar(options);

		BitSetObjectMap<BitSet> reachableByState = reachableByState(reachableByChar, options);

		return new GlushkovAutomaton(initial, finals, reachableByChar, reachableByState);
	}

	public DualGlushkovAutomaton buildReverseAutomaton(GlushkovAnalyzerOption... options) {
		BitSet initial = FACTORS.in(options) ? all() : finals();

		BitSet finals = initial();

		CharObjectMap<BitSet> emittingChar = reachableByChar(options);

		BitSetObjectMap<BitSet> reachableByState = sourceableByState(emittingChar, options);

		return new DualGlushkovAutomaton(initial, finals, emittingChar, reachableByState);
	}

	private BitSet initial() {
		BitSet initial = new BitSet(len);
		initial.set(0);
		return initial;
	}

	private CharObjectMap<BitSet> reachableByChar(GlushkovAnalyzerOption... options) {
		BitSet defaultValue = SELF_LOOP.in(options) ? initial() : new BitSet(len);

		CharObjectMap.Builder<BitSet> reachable = new CharObjectMap.Builder<BitSet>(defaultValue);
		for (int i = 1; i < len; i++) {
			for (char c : chars[i].chars()) {
				BitSet b = reachable.get(c);
				if (b == defaultValue) {
					b = (BitSet) defaultValue.clone();
					reachable.put(c, b);
				}
				b.set(i);
			}
		}
		return reachable.perfectMinimal();
	}

	private BitSetObjectMap<BitSet> reachableByState(CharObjectMap<BitSet> reachableByChar, GlushkovAnalyzerOption... options) {
		BitSet defaultValue = SELF_LOOP.in(options) ? initial() : new BitSet(len);

		BitSetObjectMap.Builder<BitSet> reachable = new BitSetObjectMap.Builder<>(defaultValue);
		reachableByState(initial(), reachable, reachableByChar, defaultValue);
		return reachable.perfectMinimal();
	}

	private void reachableByState(BitSet d, MinimalPerfectMapBuilder<BitSet, BitSet> reachable, CharObjectMap<BitSet> reachableByChar, BitSet defaultValue) {
		BitSet td = reachable.get(d);
		if (td == defaultValue) {
			td = (BitSet) defaultValue.clone();
			reachable.put(d, td);
		}
		for (int i = 0; i < len; i++) {
			if (d.get(i)) {
				td.or(bits(len, follow(i)));
			}
		}

		BitSet n = (BitSet) td.clone();
		for (char c : alphabet) {
			BitSet next = and(n, reachableByChar.get(c));
			if (reachable.get(next) == defaultValue) {
				reachableByState(next, reachable, reachableByChar, defaultValue);
			}
		}

	}

	private BitSetObjectMap<BitSet> sourceableByState(CharObjectMap<BitSet> emittingChar, GlushkovAnalyzerOption... options) {
		BitSet defaultValue = SELF_LOOP.in(options) ? initial() : new BitSet(len);

		BitSetObjectMap.Builder<BitSet> sourceable = new BitSetObjectMap.Builder<>(defaultValue);
		List<BitSet> allFinals = powerSet(finals());
		allFinals.remove(new BitSet(len));
		for (BitSet finals : allFinals) {
			sourceableByState(finals, len, sourceable, emittingChar, defaultValue);
		}
		return sourceable.perfectMinimal();
	}

	private List<BitSet> powerSet(BitSet set) {
		Queue<Integer> ints = new LinkedList<>();
		int next = -1;
		while ((next = set.nextSetBit(next + 1)) >= 0) {
			ints.add(next);
		}
		return buildPowerSet(ints, set.size());
	}

	private List<BitSet> buildPowerSet(Queue<Integer> ints, int size) {
		if (ints.isEmpty()) {
			List<BitSet> result = new ArrayList<BitSet>();
			result.add(new BitSet(size));
			return result;
		}
		int bit = ints.remove();
		List<BitSet> base = buildPowerSet(ints, size);
		List<BitSet> result = new ArrayList<BitSet>(base.size() * 2);
		for (BitSet b : base) {
			BitSet off = (BitSet) b.clone();
			result.add(off);
			BitSet on = (BitSet) b.clone();
			on.set(bit);
			result.add(on);
		}
		return result;
	}

	private void sourceableByState(BitSet d, int len, MinimalPerfectMapBuilder<BitSet, BitSet> sourceable, CharObjectMap<BitSet> emittingChar, BitSet defaultValue) {
		BitSet td = sourceable.get(d);
		if (td == defaultValue) {
			td = (BitSet) defaultValue.clone();
			sourceable.put(d, td);
		}
		for (int i = 0; i < len; i++) {
			if (d.get(i)) {
				td.or(bits(len, precede(i)));
			}
		}

		BitSet n = (BitSet) td.clone();
		for (char c : alphabet) {
			BitSet next = and(n, emittingChar.get(c));
			if (sourceable.get(next) == defaultValue) {
				sourceableByState(next, len, sourceable, emittingChar, defaultValue);
			}
		}

	}

	private BitSet bits(int len, Set<Integer> ints) {
		BitSet bits = new BitSet(len);
		for (int i : ints) {
			bits.set(i);
		}
		return bits;
	}

	private BitSet finals() {
		BitSet finals = new BitSet(len);
		for (int x : last(root)) {
			finals.set(x);
		}
		if (empty(root)) {
			finals.set(0);
		}
		return finals;
	}

	private BitSet all() {
		BitSet all = new BitSet(len);
		all.flip(0, len);
		return all;
	}

	@Override
	public Void visitAlternatives(AlternativesNode node) {
		List<RegexNode> subNodes = node.getSubNodes();
		for (RegexNode subNode : subNodes) {
			subNode.accept(this);
		}

		first(node, union(first(subNodes)));

		last(node, union(last(subNodes)));

		empty(node, or(empty(subNodes)));

		return null;
	}

	@Override
	public Void visitAnyChar(AnyCharNode node) {
		throw new UnsupportedOperationException("decomposed normal from does not contain char classes");
	}

	@Override
	public Void visitCharClass(CharClassNode node) {
		throw new UnsupportedOperationException("decomposed normal from does not contain char classes");
	}

	@Override
	public Void visitCompClass(CompClassNode node) {
		throw new UnsupportedOperationException("decomposed normal from does not contain char classes");
	}

	@Override
	public Void visitConcat(ConcatNode node) {
		List<RegexNode> subNodes = node.getSubNodes();
		for (RegexNode subNode : subNodes) {
			subNode.accept(this);
		}

		first(node, union(concatFirst(subNodes)));

		last(node, union(concatLast(subNodes)));

		empty(node, and(empty(subNodes)));

		for (int i = 0; i < subNodes.size() - 1; i++) {
			RegexNode current = subNodes.get(i);
			RegexNode next = subNodes.get(i + 1);
			for (int x : last(current)) {
				appendFollow(x, first(next));
			}
			for (int y : first(next)) {
				appendPrecede(y, last(current));
			}
		}

		return null;
	}

	private List<Set<Integer>> concatFirst(List<RegexNode> subNodes) {
		List<Set<Integer>> result = new ArrayList<>();
		boolean empty = true;
		for (RegexNode subNode : subNodes) {
			if (empty) {
				result.add(first(subNode));
			}
			empty &= empty(subNode);
		}
		return result;
	}

	private List<Set<Integer>> concatLast(List<RegexNode> subNodes) {
		List<RegexNode> reverseSubNodes = new ArrayList<>(subNodes);
		Collections.reverse(reverseSubNodes);
		List<Set<Integer>> result = new ArrayList<>();
		boolean empty = true;
		for (RegexNode subNode : reverseSubNodes) {
			if (empty) {
				result.add(last(subNode));
			}
			empty &= empty(subNode);
		}
		return result;
	}

	@Override
	public Void visitEmpty(EmptyNode node) {
		first(node, new HashSet<Integer>());

		last(node, new HashSet<Integer>());

		empty(node, true);

		return null;
	}

	@Override
	public Void visitGroup(GroupNode node) {
		RegexNode subNode = node.getSubNode();
		subNode.accept(this);

		first(node, first(subNode));

		last(node, last(subNode));

		empty(node, empty(subNode));

		return null;
	}

	@Override
	public Void visitBoundedLoop(BoundedLoopNode node) {
		throw new UnsupportedOperationException("decomposed normal from does not contain bounded loops");
	}

	@Override
	public Void visitUnboundedLoop(UnboundedLoopNode node) {
		if (node.getFrom() > 0) {
			throw new UnsupportedOperationException("decomposed normal from does not contain plus loops");
		}
		RegexNode subNode = node.getSubNode();
		subNode.accept(this);

		first(node, first(subNode));

		last(node, last(subNode));

		empty(node, true);

		RegexNode current = subNode;
		RegexNode next = subNode;
		for (int x : last(current)) {
			appendFollow(x, first(next));
		}
		for (int y : first(next)) {
			appendPrecede(y, last(current));
		}
		return null;
	}

	@Override
	public Void visitOptional(OptionalNode node) {
		RegexNode subNode = node.getSubNode();
		subNode.accept(this);

		first(node, first(subNode));

		last(node, last(subNode));

		empty(node, true);

		return null;
	}

	@Override
	public Void visitRangeChar(RangeCharNode node) {
		int pos = charCollector.size();
		charCollector.add(node);

		first(node, pos);

		last(node, pos);

		empty(node, false);

		return null;
	}

	@Override
	public Void visitSingleChar(SingleCharNode node) {
		int pos = charCollector.size();
		charCollector.add(node);

		first(node, pos);

		last(node, pos);

		empty(node, false);

		return null;
	}

	@Override
	public Void visitSpecialCharClass(SpecialCharClassNode node) {
		throw new UnsupportedOperationException("decomposed normal from does not contain char classes");
	}

	@Override
	public Void visitString(StringNode node) {
		throw new UnsupportedOperationException("decomposed normal from does not contain strings");
	}

	private Set<Integer> union(List<Set<Integer>> values) {
		Set<Integer> result = new LinkedHashSet<>();
		for (Set<Integer> value : values) {
			result.addAll(value);
		}
		return result;
	}

	private Boolean or(List<Boolean> values) {
		boolean result = false;
		for (Boolean value : values) {
			result |= value;
		}
		return result;
	}

	private Boolean and(List<Boolean> values) {
		boolean result = true;
		for (Boolean value : values) {
			result &= value;
		}
		return result;
	}

	private BitSet and(BitSet b1, BitSet b2) {
		BitSet bitSet = (BitSet) b1.clone();
		bitSet.and(b2);
		return bitSet;
	}

}
