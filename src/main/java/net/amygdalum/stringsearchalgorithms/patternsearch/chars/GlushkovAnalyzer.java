package net.amygdalum.stringsearchalgorithms.patternsearch.chars;

import static java.util.Arrays.asList;
import static net.amygdalum.stringsearchalgorithms.patternsearch.chars.GlushkovAnalyzerOption.FACTORS;
import static net.amygdalum.stringsearchalgorithms.patternsearch.chars.GlushkovAnalyzerOption.SELF_LOOP;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.amygdalum.stringsearchalgorithms.regex.AlternativesNode;
import net.amygdalum.stringsearchalgorithms.regex.AnyCharNode;
import net.amygdalum.stringsearchalgorithms.regex.BoundedLoopNode;
import net.amygdalum.stringsearchalgorithms.regex.CharClassNode;
import net.amygdalum.stringsearchalgorithms.regex.CompClassNode;
import net.amygdalum.stringsearchalgorithms.regex.ConcatNode;
import net.amygdalum.stringsearchalgorithms.regex.DefinedCharNode;
import net.amygdalum.stringsearchalgorithms.regex.EmptyNode;
import net.amygdalum.stringsearchalgorithms.regex.GroupNode;
import net.amygdalum.stringsearchalgorithms.regex.OptionalNode;
import net.amygdalum.stringsearchalgorithms.regex.RangeCharNode;
import net.amygdalum.stringsearchalgorithms.regex.RegexNode;
import net.amygdalum.stringsearchalgorithms.regex.RegexNodeVisitor;
import net.amygdalum.stringsearchalgorithms.regex.SingleCharNode;
import net.amygdalum.stringsearchalgorithms.regex.SpecialCharClassNode;
import net.amygdalum.stringsearchalgorithms.regex.StringNode;
import net.amygdalum.stringsearchalgorithms.regex.UnboundedLoopNode;
import net.amygdalum.util.map.BitSetObjectMap;
import net.amygdalum.util.map.CharObjectMap;

public class GlushkovAnalyzer implements RegexNodeVisitor<Void> {

	private RegexNode root;
	private List<DefinedCharNode> charCollector;
	private Map<RegexNode, Set<Integer>> first;
	private Map<RegexNode, Set<Integer>> last;
	private Map<Integer, Set<Integer>> follow;
	private Map<Integer, Set<Integer>> precede;
	private Map<RegexNode, Integer> minLength;
	private DefinedCharNode[] chars;
	private int len;
	private char[] alphabet;

	public GlushkovAnalyzer(RegexNode root) {
		this.root = root;
		this.first = new LinkedHashMap<>();
		this.last = new LinkedHashMap<>();
		this.follow = new LinkedHashMap<>();
		this.precede = new LinkedHashMap<>();
		this.minLength = new LinkedHashMap<>();
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

	public Set<Character> firstChars() {
		Set<Character> firstChars = new LinkedHashSet<>();
		for (int index : first(root)) {
			for (char c : chars[index].chars()) {
				firstChars.add(c);
			}
		}
		return firstChars;
	}

	public Set<Character> lastChars() {
		Set<Character> lastChars = new LinkedHashSet<>();
		for (int index : last(root)) {
			for (char c : chars[index].chars()) {
				lastChars.add(c);
			}
		}
		return lastChars;
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

	private void minLength(RegexNode node, Integer value) {
		minLength.put(node, value);
	}

	private List<Integer> minLength(List<RegexNode> nodes) {
		List<Integer> result = new ArrayList<>(nodes.size());
		for (RegexNode node : nodes) {
			result.add(minLength(node));
		}
		return result;
	}

	private Integer minLength(RegexNode node) {
		return minLength.get(node);
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

		CharObjectMap<BitSet> reachableByChar = reachableByChar(options);

		BitSetObjectMap<BitSet> reachableByState = sourceableByState(reachableByChar, options);

		return new DualGlushkovAutomaton(initial, finals, reachableByChar, reachableByState);
	}

	public int minLength() {
		return minLength(root);
	}

	private BitSet initial() {
		BitSet initial = new BitSet(len);
		initial.set(0);
		return initial;
	}

	private CharObjectMap<BitSet> reachableByChar(GlushkovAnalyzerOption... options) {
		BitSet defaultValue = SELF_LOOP.in(options) ? initial() : new BitSet(len);

		CharObjectMap<BitSet> reachable = new CharObjectMap<BitSet>(defaultValue);
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
		return reachable;
	}

	private BitSetObjectMap<BitSet> reachableByState(CharObjectMap<BitSet> reachableByChar, GlushkovAnalyzerOption... options) {
		BitSet defaultValue = SELF_LOOP.in(options) ? initial() : new BitSet(len);
		BitSet start = FACTORS.in(options) ? all() : initial();

		BitSetObjectMap<BitSet> reachable = new BitSetObjectMap<>(defaultValue);
		reachableByState(start, reachable, reachableByChar, defaultValue);
		return reachable;
	}

	private void reachableByState(BitSet d, BitSetObjectMap<BitSet> reachable, CharObjectMap<BitSet> reachableByChar, BitSet defaultValue) {
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

	private BitSetObjectMap<BitSet> sourceableByState(CharObjectMap<BitSet> reachableByChar, GlushkovAnalyzerOption... options) {
		BitSet defaultValue = SELF_LOOP.in(options) ? finals() : new BitSet(len);
		BitSet start = FACTORS.in(options) ? all() : finals();

		BitSetObjectMap<BitSet> sourceable = new BitSetObjectMap<>(defaultValue);
		List<BitSet> allFinals = allFinals(start, reachableByChar, options);
		for (BitSet finals : allFinals) {
			sourceableByState(finals, len, sourceable, reachableByChar, defaultValue);
		}
		return sourceable;
	}

	private List<BitSet> allFinals(BitSet initial, CharObjectMap<BitSet> reachableByChar, GlushkovAnalyzerOption... options) {
		BitSet start = FACTORS.in(options) ? all() : initial();
		BitSet defaultValue = SELF_LOOP.in(options) ? initial() : new BitSet(len);
		
		Collection<BitSet> possible = possibleStartsByState(start, reachableByChar, defaultValue);
		
		return filterPossiblesStartsByChar(initial, reachableByChar, possible);
	}

	private Collection<BitSet> possibleStartsByState(BitSet next, CharObjectMap<BitSet> reachableByChar, BitSet defaultValue) {
		Map<BitSet, BitSet> possible = new LinkedHashMap<>();
		possibleStartsByState(possible, next, reachableByChar, defaultValue);
		return possible.values();
	}

	private void possibleStartsByState(Map<BitSet, BitSet> possible, BitSet next, CharObjectMap<BitSet> reachableByChar, BitSet defaultValue) {
		if (possible.get(next) == null) {
			possibleStartsByState(next, possible, reachableByChar, defaultValue);
		}
		next = or(next, initial());
		if (possible.get(next) == null) {
			possibleStartsByState(next, possible, reachableByChar, defaultValue);
		}
	}

	private void possibleStartsByState(BitSet d, Map<BitSet, BitSet> possible, CharObjectMap<BitSet> reachableByChar, BitSet defaultValue) {
		BitSet td = possible.get(d);
		if (td == null) {
			td = (BitSet) defaultValue.clone();
			possible.put(d, td);
		}
		for (int i = 0; i < len; i++) {
			if (d.get(i)) {
				td.or(bits(len, follow(i)));
			}
		}
		BitSet n = (BitSet) td.clone();
		for (char c : alphabet) {
			BitSet next = and(n, reachableByChar.get(c));
			possibleStartsByState(possible, next, reachableByChar, defaultValue);
		}
	}

	private List<BitSet> filterPossiblesStartsByChar(BitSet initial, CharObjectMap<BitSet> reachableByChar, Collection<BitSet> possible) {
		Set<BitSet> filteredPossible = new LinkedHashSet<>();
		for (BitSet value : possible) {
			BitSet finalValue = (BitSet) initial.clone();
			finalValue.and(value);
			for (char c : alphabet) {
				BitSet charFilter = reachableByChar.get(c);
				BitSet state = and(finalValue, charFilter);
				if (!state.isEmpty()) {
					filteredPossible.add(state);
				}
			}
		}
		return new ArrayList<>(filteredPossible);
	}

	private void sourceableByState(BitSet d, int len, BitSetObjectMap<BitSet> sourceable, CharObjectMap<BitSet> reachableByChar, BitSet defaultValue) {
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
			BitSet next = and(n, reachableByChar.get(c));
			if (sourceable.get(next) == defaultValue) {
				sourceableByState(next, len, sourceable, reachableByChar, defaultValue);
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
		if (minLength.get(root) == 0) {
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

		minLength(node, minimum(minLength(subNodes)));

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

		minLength(node, sum(minLength(subNodes)));

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
		int minLength = 0;
		for (RegexNode subNode : subNodes) {
			if (minLength > 0) {
				break;
			}
			result.add(first(subNode));
			minLength += minLength(subNode);
		}
		return result;
	}

	private List<Set<Integer>> concatLast(List<RegexNode> subNodes) {
		List<RegexNode> reverseSubNodes = new ArrayList<>(subNodes);
		Collections.reverse(reverseSubNodes);
		List<Set<Integer>> result = new ArrayList<>();
		int minLength = 0;
		for (RegexNode subNode : reverseSubNodes) {
			if (minLength > 0) {
				break;
			}
			result.add(last(subNode));
			minLength += minLength(subNode);
		}
		return result;
	}

	@Override
	public Void visitEmpty(EmptyNode node) {
		first(node, new HashSet<Integer>());

		last(node, new HashSet<Integer>());

		minLength(node, 0);

		return null;
	}

	@Override
	public Void visitGroup(GroupNode node) {
		RegexNode subNode = node.getSubNode();
		subNode.accept(this);

		first(node, first(subNode));

		last(node, last(subNode));

		minLength(node, minLength(subNode));

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

		minLength(node, 0);

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

		minLength(node, 0);

		return null;
	}

	@Override
	public Void visitRangeChar(RangeCharNode node) {
		int pos = charCollector.size();
		charCollector.add(node);

		first(node, pos);

		last(node, pos);

		minLength(node, 1);

		return null;
	}

	@Override
	public Void visitSingleChar(SingleCharNode node) {
		int pos = charCollector.size();
		charCollector.add(node);

		first(node, pos);

		last(node, pos);

		minLength(node, 1);

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

	private Integer minimum(List<Integer> values) {
		int min = Integer.MAX_VALUE;
		for (Integer value : values) {
			if (value < min) {
				min = value;
			}
		}
		return min;
	}

	private Integer sum(List<Integer> values) {
		int sum = 0;
		for (Integer value : values) {
			sum += value;
		}
		return sum;
	}

	private BitSet and(BitSet b1, BitSet b2) {
		BitSet bitSet = (BitSet) b1.clone();
		bitSet.and(b2);
		return bitSet;
	}

	private BitSet or(BitSet b1, BitSet b2) {
		BitSet bitSet = (BitSet) b1.clone();
		bitSet.or(b2);
		return bitSet;
	}

}
