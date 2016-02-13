package com.almondtools.util.map;

import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;

public class HashFunctionTest {

	@Test
	public void testSeed17Into9() throws Exception {
		Set<long[]> set = setArrayOf(153, 409, 1);

		assertThat(set, noDuplicatesData(17, 9));
	}

	@Test
	public void testSeed17Into26() throws Exception {
		Set<long[]> set = setArrayOf(
			256, 392, 152, 16, 264, 384, 144, 24, 4, 64, 2, 32, 408, 272, 34, 68, 136, 400, 280, 8, 128);

		assertThat(set, noDuplicatesData(17, 26));
	}

	@Test
	public void testSeed17Into11() throws Exception {
		Set<long[]> set = setArrayOf(64, 4, 32, 2, 1, 384, 144, 8, 128);

		assertThat(set, noDuplicatesData(17, 11));
	}
	
	@Test
	public void testSeed17Into6() throws Exception {
		Set<long[]> set = setArrayOf(4, 2, 16, 1, 8);

		assertThat(set, noDuplicatesData(17, 6));
	}
	
	@Test
	public void testSeed17Into4() throws Exception {
		Set<Character> set = setOf('a','b','\u0262');

		assertThat(set, noDuplicatesChar(17, 4));
	}

	private Matcher<Set<Character>> noDuplicatesChar(final int seed, final int size) {
		return new TypeSafeDiagnosingMatcher<Set<Character>>() {

			@Override
			protected boolean matchesSafely(Set<Character> data, Description mismatchDescription) {
				Random r = new Random(seed);

				nextSeed: for (int i = 0; i < 100; i++) {
					int seed1 = r.nextInt();
					int seed2 = r.nextInt();
					HashFunction f = new HashFunction(new int[size], seed1, seed2);

					int[][] edges = new int[data.size()][];
					int p = 0;
					for (char c : data) {
						edges[p] = f.doubleHash((int) c);
						p++;
					}

					for (int j = 0; j < edges.length; j++) {
						for (int k = j + 1; k < edges.length; k++) {
							int[] normalizedJ = order(edges[j]);
							int[] normalizedK = order(edges[k]);
							if (normalizedJ[0] == normalizedK[0] && normalizedJ[1] == normalizedK[1]) {
								mismatchDescription.appendText(Arrays.toString(normalizedJ) + ":" + Arrays.toString(normalizedK) + " was duplicate\n");
								continue nextSeed;
							}
						}
					}
					return true;
				}
				return false;
			}

			private int[] order(int[] edge) {
				if (edge[0] > edge[1]) {
					return new int[] { edge[1], edge[0] };
				} else {
					return edge;
				}
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("not containing duplicates");
			}

		};
	}

	private Matcher<Set<long[]>> noDuplicatesData(final int seed, final int size) {
		return new TypeSafeDiagnosingMatcher<Set<long[]>>() {

			@Override
			protected boolean matchesSafely(Set<long[]> data, Description mismatchDescription) {
				Random r = new Random(seed);

				nextSeed: for (int i = 0; i < 100; i++) {
					int seed1 = r.nextInt();
					int seed2 = r.nextInt();
					HashFunction f = new HashFunction(new int[size], seed1, seed2);

					int[][] edges = new int[data.size()][];
					int p = 0;
					for (long[] d : data) {
						edges[p] = f.doubleHash(d);
						p++;
					}

					for (int j = 0; j < edges.length; j++) {
						for (int k = j + 1; k < edges.length; k++) {
							int[] normalizedJ = order(edges[j]);
							int[] normalizedK = order(edges[k]);
							if (normalizedJ[0] == normalizedK[0] && normalizedJ[1] == normalizedK[1]) {
								mismatchDescription.appendText(Arrays.toString(normalizedJ) + ":" + Arrays.toString(normalizedK) + " was duplicate\n");
								continue nextSeed;
							}
						}
					}
					return true;
				}
				return false;
			}

			private int[] order(int[] edge) {
				if (edge[0] > edge[1]) {
					return new int[] { edge[1], edge[0] };
				} else {
					return edge;
				}
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("not containing duplicates");
			}

		};
	}

	private Set<Character> setOf(char... chars) {
		Set<Character> set = new HashSet<>();
		for (char c : chars) {
			set.add(c);
		}
		return set;
	}

	private Set<long[]> setArrayOf(long... data) {
		Set<long[]> set = new HashSet<>();
		for (long d : data) {
			set.add(new long[] { d });
		}
		return set;
	}

}
