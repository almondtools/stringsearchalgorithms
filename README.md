StringsAndChars
===============
StringAndChars is a library for string matching algorithms.

String Matching with Java SDK API
---------------------------------
The Java SDK proposes two approaches to search for strings efficiently:

* `String.indexOf(...)` and `String.lastIndexOf(...)`
* `Pattern.compile()` together with `Matcher.find`

*Search for one word in one text*: `String.indexOf` is fine for small patterns but it uses the naive string matching implementation. `Pattern.compile/Matcher.find` with constant strings uses a variant of the Boyer-Moore string-matching algorithm, which is more efficient. Performance gain beyond Boyer-Moore is limited (some StringsAndChars algorithms perform better, but the basic runtime characteristics are comparable). 

*Search for multiple words in one text*: `String.indexOf` and `Pattern.compile/Matcher.find` both switch to naive string matching. The runtime of the different StringsAndChars algorithms perform better than this. The overall performance of the algorithms is dependent on  pattern size and alphabet size (each algorithm has its own preferred region of application). 

Conclusion
----------
The problem of searching one string in a large texts can be done efficiently with the Java SDK API. It is a bit tricky to find out that the regex API covers an efficient string matching algorithm for single strings and that the default string search with `indexOf` does not.

The problem of searching multiple strings in large texts is not properly covered by the Java SDK API. StringsAndChars provides miscellaneous algorithms to solve this problem. Which algorithm is best depends on the alphabet size and the average pattern size.


String Matching
===============
The string matching algorithms can be found in the package `com.almondtools.stringsandchars.search`:

Search one string:
 - Boyer-Moore-Horspool or Horspool (`Horspool`)
 - Shift-And (`ShiftAnd`)
 - Knuth-Morris-Pratt (`KnuthMorrisPratt`)
 - Sunday (`Sunday`)
 - BNDM (`BNDM`)

Search multiple strings:
 - Set-Backward-Oracle-Matching (`SetBackwardOracleMatching`)
 - Aho-Corasick (`AhoCorasick`)
 - Set-Horspool (`SetHorspool`)
 - Wu-Manber (`WuManber`)

Now first initialize the Algorithm with the pattern:

```Java
	Horspool stringSearch = new Horspool("wordToSearch");
```

Then create a finder from the algorithm and provide the text you want to search in:

```Java
	StringFinder finder = stringSearch.createFinder(new StringCharProvider("text with wordToSearch in it", 0));
```

You can now find all occurrences of the pattern

```Java
	List<StringMatch> all = finder.findAll();
```

or all non overlapping :

```Java
	List<StringMatch> all = finder.findAllNonOverlapping();
```

or the next one:

```Java
	StringMatch first = finder.findNext();
```

Maven Dependency
----------------

```xml
<dependency>
	<groupId>com.github.almondtools</groupId>
	<artifactId>stringsandchars</artifactId>
	<version>0.2.5</version>
</dependency>
```

Todos
-----
- Extended String Matching
