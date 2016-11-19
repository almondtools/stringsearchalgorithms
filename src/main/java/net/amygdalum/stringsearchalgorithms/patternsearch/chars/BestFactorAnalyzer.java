package net.amygdalum.stringsearchalgorithms.patternsearch.chars;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.amygdalum.stringsearchalgorithms.patternsearch.chars.BestFactorAnalyzer.Factors;
import net.amygdalum.stringsearchalgorithms.regex.AlternativesNode;
import net.amygdalum.stringsearchalgorithms.regex.AnyCharNode;
import net.amygdalum.stringsearchalgorithms.regex.BoundedLoopNode;
import net.amygdalum.stringsearchalgorithms.regex.CharClassNode;
import net.amygdalum.stringsearchalgorithms.regex.CompClassNode;
import net.amygdalum.stringsearchalgorithms.regex.ConcatNode;
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

public class BestFactorAnalyzer implements RegexNodeVisitor<Factors> {

	private RegexNode root;
	private Factors factors;

	public BestFactorAnalyzer(RegexNode root) {
		this.root = root;
	}

	public Set<String> getBestFactors(Set<String> default1, Set<String> default2) {
		return bestOf(factors.prefix, factors.suffix, factors.factor, default1, default2);
	}

	public BestFactorAnalyzer analyze() {
		factors = root.accept(this);
		return this;
	}

	@SafeVarargs
	private static final Set<String> bestOf(Set<String>... sets) {
		double maxScore = 0d;
		Set<String> resultSet = null;
		for (Set<String> set : sets) {
			double score = score(set);
			if (score > maxScore) {
				resultSet = set;
				maxScore = score;
			} else if (score == maxScore && set.size() < resultSet.size()) {
				resultSet = set;
			}
		}
		return resultSet;
	}

	private static double score(Set<String> set) {
		if (set == null) {
			return -1d;
		}
		double size = set.size();
		double sum = 0d;
		for (String string : set) {
			sum += string.length();
		}
		return sum / size;
	}

	@Override
	public Factors visitAlternatives(AlternativesNode node) {
		List<Factors> factors = accept(node.getSubNodes());

		return Factors.alternative(factors);
	}

	@Override
	public Factors visitAnyChar(AnyCharNode node) {
		List<Factors> factors = accept(node.toCharNodes());
		return Factors.alternative(factors);
	}

	@Override
	public Factors visitCharClass(CharClassNode node) {
		List<Factors> factors = accept(node.toCharNodes());
		return Factors.alternative(factors);
	}

	@Override
	public Factors visitCompClass(CompClassNode node) {
		List<Factors> factors = accept(node.toCharNodes());
		return Factors.alternative(factors);
	}

	@Override
	public Factors visitConcat(ConcatNode node) {
		List<Factors> factors = accept(node.getSubNodes());

		return Factors.concat(factors);
	}

	@Override
	public Factors visitEmpty(EmptyNode node) {
		return Factors.empty();
	}

	@Override
	public Factors visitGroup(GroupNode node) {
		return node.getSubNode().accept(this);
	}

	@Override
	public Factors visitBoundedLoop(BoundedLoopNode node) {
		Factors factor = node.getSubNode().accept(this);

		return Factors.variableSequence(factor, node.getFrom(), node.getTo());
	}

	@Override
	public Factors visitUnboundedLoop(UnboundedLoopNode node) {
		return Factors.invalid();
	}

	@Override
	public Factors visitOptional(OptionalNode node) {
		Factors factors = node.getSubNode().accept(this);

		return Factors.optional(factors);
	}

	@Override
	public Factors visitRangeChar(RangeCharNode node) {
		Set<String> factors = new LinkedHashSet<>();

		for (char c = node.getFrom(); c <= node.getTo(); c++) {
			factors.add(String.valueOf(c));
		}
		return Factors.base(factors);
	}

	@Override
	public Factors visitSingleChar(SingleCharNode node) {
		return Factors.string(node.getLiteralValue());
	}

	@Override
	public Factors visitSpecialCharClass(SpecialCharClassNode node) {
		List<Factors> factors = accept(node.toCharNodes());
		return Factors.alternative(factors);
	}

	@Override
	public Factors visitString(StringNode node) {
		return Factors.string(node.getLiteralValue());
	}

	private List<Factors> accept(List<? extends RegexNode> subNodes) {
		List<Factors> factors = new ArrayList<Factors>(subNodes.size());
		for (RegexNode subNode : subNodes) {
			factors.add(subNode.accept(this));
		}
		return factors;
	}

	static class Factors {
		public Set<String> all;
		public Set<String> prefix;
		public Set<String> suffix;
		public Set<String> factor;

		public Factors(Set<String> all, Set<String> prefix, Set<String> suffix, Set<String> factor) {
			this.all = all;
			this.prefix = prefix;
			this.suffix = suffix;
			this.factor = factor;
		}

		@Override
		public String toString() {
			return "{\n"
				+ "all    :" + all + "\n"
				+ "prefix :" + prefix + "\n"
				+ "suffix :" + suffix + "\n"
				+ "factor :" + factor + "\n"
				+ "}";
		}

		private static Set<String> concat(Set<String> prefixes, Set<String> suffixes) {
			if (prefixes == null || suffixes == null) {
				return null;
			}
			Set<String> concat = new LinkedHashSet<>();
			for (String prefix : prefixes) {
				for (String suffix : suffixes) {
					concat.add(prefix + suffix);
				}
			}
			return concat;
		}

		public static Factors alternative(List<Factors> factors) {
			Builder builder = new Factors.Builder();

			for (Factors factorsAlternative : factors) {
				builder.addAll(factorsAlternative.all);
				builder.addPrefix(factorsAlternative.prefix);
				builder.addSuffix(factorsAlternative.suffix);
				builder.addFactor(factorsAlternative.factor);
			}

			return builder.build();
		}

		public static Factors concat(List<Factors> factors) {
			Iterator<Factors> factorsIterator = factors.iterator();
			if (factorsIterator.hasNext()) {
				Factors factorsConcat = factorsIterator.next();
				Builder builder = new Factors.Builder(factorsConcat);
				while (factorsIterator.hasNext()) {
					factorsConcat = factorsIterator.next();

					Set<String> newPrefix = concat(builder.all, factorsConcat.prefix);
					Set<String> newSuffix = concat(builder.suffix, factorsConcat.all);
					Set<String> newFactor = concat(builder.prefix, factorsConcat.suffix);
					
					builder.addAll(factorsConcat.all);
					builder.updatePrefix(builder.prefix, newPrefix);
					builder.updateSuffix(newSuffix, factorsConcat.suffix);
					builder.updateFactor(builder.factor, factorsConcat.factor, newFactor);
				}
				return builder.build();
			} else {
				return Factors.empty();
			}
		}

		public static Factors variableSequence(Factors factor, int from, int to) {
			Factors result = factor;
			for (int i = 0; i < from; i++) {
				result = Factors.concat(asList(result, factor));
			}
			for (int i = from; i < to; i++) {
				result = Factors.concat(asList(result, Factors.alternative(asList(factor, Factors.empty()))));
			}

			return result;
		}

		public static Factors optional(Factors factors) {
			return Factors.alternative(asList(factors, empty()));
		}

		public static Factors base(Set<String> factors) {
			return new Factors(factors, factors, factors, factors);
		}

		public static Factors string(String factor) {
			Set<String> factors = new HashSet<>();
			factors.add(factor);
			return base(factors);
		}

		public static Factors empty() {
			Set<String> empty = new HashSet<>();
			empty.add("");
			return base(empty);
		}

		public static Factors invalid() {
			return base(null);
		}

		public static class Builder {

			private Set<String> all;
			private Set<String> prefix;
			private Set<String> suffix;
			private Set<String> factor;

			public Builder() {
				all = new LinkedHashSet<>();
				prefix = new LinkedHashSet<>();
				suffix = new LinkedHashSet<>();
				factor = new LinkedHashSet<>();
			}

			public Builder(Factors prototype) {
				all = prototype.all ==  null ? null : new LinkedHashSet<>(prototype.all);
				prefix = prototype.prefix ==  null ? null : new LinkedHashSet<>(prototype.prefix);
				suffix = prototype.suffix ==  null ? null : new LinkedHashSet<>(prototype.suffix);
				factor = prototype.factor ==  null ? null : new LinkedHashSet<>(prototype.factor);
			}

			public void addAll(Set<String> factors) {
				if (all == null) {
					return;
				}
				if (factors == null) {
					all = null;
				} else {
					all.addAll(factors);
				}
			}

			public void addPrefix(Set<String> factors) {
				if (prefix == null) {
					return;
				}
				if (factors == null) {
					prefix = null;
				} else {
					prefix.addAll(factors);
				}
			}

			@SafeVarargs
			public final void updatePrefix(Set<String>... factors) {
				prefix = bestOf(factors);
			}

			public void addSuffix(Set<String> factors) {
				if (suffix == null) {
					return;
				}
				if (factors == null) {
					suffix = null;
				} else {
					suffix.addAll(factors);
				}
			}

			@SafeVarargs
			public final void updateSuffix(Set<String>... factors) {
				suffix = bestOf(factors);
			}

			public void addFactor(Set<String> factors) {
				if (factor == null) {
					return;
				}
				if (factors == null) {
					factor = null;
				} else {
					factor.addAll(factors);
				}
			}

			@SafeVarargs
			public final void updateFactor(Set<String>... factors) {
				factor = bestOf(factors);
			}

			public Factors build() {
				return new Factors(all, prefix, suffix, factor);
			}

		}

	}

}
