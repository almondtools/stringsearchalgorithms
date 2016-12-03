package net.amygdalum.util.text;

import java.util.List;

public class QGramAlphabet {

	private QGramMapping mapping;
	private int minQGram;
	private int maxQGram;

	private QGramAlphabet(QGramMapping mapping) {
		this.mapping = mapping;
		this.minQGram = Integer.MAX_VALUE;
		this.maxQGram = 0;
	}

	public static QGramAlphabet of(List<char[]> patterns, QGramMapping mapping) {
		QGramAlphabet qgramAlphabet = new QGramAlphabet(mapping);
		for (char[] pattern : patterns) {
			for (int qc : mapping.iterate(pattern)) {
				qgramAlphabet.add(qc);
			}
		}
		return qgramAlphabet;
	}
	
	public QGramMapping getMapping() {
		return mapping;
	}

	public void add(int qc) {
		if (qc < minQGram) {
			minQGram = qc;
		}
		if (qc > maxQGram) {
			maxQGram = qc;
		}
	}

	public int minQGram() {
		return minQGram;
	}

	public int maxQGram() {
		return maxQGram;
	}

}
