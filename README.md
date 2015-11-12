StringsAndChars
===============
StringAndChars is a library supporting search of String and Chars together with some tools on String/Char Processing

String Search
=============
Rexlex provides a bunch of string search algorithms which could easily be used without the regular expressions. The algorithms can be found in the package 
`com.almondtools.rexlex.stringsearch`:

Search one string:
 - Horspool (default)
 - KnuthMorrisPratt

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
	<version>0.1.0</version>
</dependency>
```
