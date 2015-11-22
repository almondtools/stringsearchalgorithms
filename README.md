StringsAndChars
===============
StringAndChars is a library supporting search of String and Chars together with some tools on String/Char processing. The core of this library are different string search algorithms.

String Search with Java SDK API
-------------------------------
The Java SDK proposes two approaches to search for strings efficiently:

* `String.indexOf(...)` and `String.lastIndexOf(...)`
* `Pattern.compile()` together with `Matcher.find`

*Search for one word in one text*: `String.indexOf` is fine for small patterns but it uses the naive implementation causing a runtime of O(m*n) (where m is the pattern length and n is the length of the text to search in). `Pattern.compile/Matcher.find` with constant strings uses a variant of the boyer-moore-string-matching algorithm, which has sublinear runtime. So the search for a single pattern could be done efficiently with the Java SDK. The best algorithms in StringsAndChars are faster, but the performance boost is only small.

*Search for multiple words in one text*: `String.indexOf` and `Pattern.compile/Matcher.find` both switch to naive string search causing a runtime of O(m*n) (where m is the sum of all pattern lengths and n is the length of the text to search in).

This means: As long as string matching occurs only sometimes and affects only small patterns and small texts,  there is no need to use more efficient algorithm. The algorithms in StringsAndChars are meant for string matching that is done often where the texts are probably very long.
       

String Search
=============
The algorithms for searching strings in large texts can be found in the package `com.almondtools.stringsandchars.search`:

Search one string:
 - Horspool (default)
 - KnuthMorrisPratt
 - Sunday

Search multiple strings:
 - SetBackwardOracleMatching (default)
 - AhoCorasick
 - SetHorspool
 - WuManber

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
	<version>0.2.3</version>
</dependency>
```

Todos
-----
- Horspool/Sunday/SetHorspool's memory consumption should not be linear dependent on the used alphabet size
