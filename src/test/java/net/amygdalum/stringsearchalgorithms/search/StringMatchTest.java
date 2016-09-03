package net.amygdalum.stringsearchalgorithms.search;

import static com.almondtools.conmatch.conventions.EqualityMatcher.satisfiesDefaultEquality;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.amygdalum.stringsearchalgorithms.search.StringMatch;


public class StringMatchTest {

	@Test
	public void testStart() throws Exception {
		assertThat(new StringMatch(0, 2, "ab").start(), equalTo(0l));
	}
	
	@Test
	public void testEnd() throws Exception {
		assertThat(new StringMatch(0, 2, "ab").end(), equalTo(2l));
	}
	
	@Test
	public void testText() throws Exception {
		assertThat(new StringMatch(0, 2, "ab").text(), equalTo("ab"));
	}

	@Test
	public void testIsEmptyIfStartEqualsEnd() throws Exception {
		assertThat(new StringMatch(0, 0, "").isEmpty(), is(true));
	}

	@Test
	public void testIsNotEmptyIfStartLessThanEnd() throws Exception {
		assertThat(new StringMatch(0, 1, "a").isEmpty(), is(false));
	}

	@Test
	public void testCompareTo() throws Exception {
		assertThat(new StringMatch(0,2,"ab"), lessThan(new StringMatch(1,2,"b")));
		assertThat(new StringMatch(0,2,"ab"), lessThan(new StringMatch(0,3,"abc")));
		assertThat(new StringMatch(1,2,"b"), greaterThan(new StringMatch(0,2,"ab")));
		assertThat(new StringMatch(0,3,"b"), greaterThan(new StringMatch(0,2,"ab")));
		assertThat(new StringMatch(0,2,"ab"), comparesEqualTo(new StringMatch(0,2,"cd")));
	}

	@Test
	public void testToString() throws Exception {
		assertThat(new StringMatch(0, 2, "ab").toString(), equalTo("0:2(ab)"));
	}

	@Test
	public void testEquals() throws Exception {
		assertThat(new StringMatch(0, 2, "ab"), satisfiesDefaultEquality()
			.andEqualTo(new StringMatch(0,2,"ab"))
			.andNotEqualTo(new StringMatch(0,1,"ab"))
			.andNotEqualTo(new StringMatch(1,2,"ab"))
			.andNotEqualTo(new StringMatch(0,2,"abc")));
	}
	
}
