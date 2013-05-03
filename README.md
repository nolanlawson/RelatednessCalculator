Relatedness Calculator
=========================

Current version: 1.0

Developer
-----------

Nolan Lawson

License
-----------

[WTFPL][1], but attribution would be nice.

Summary
------------

![Screenshot][5]

Java library for calculating the genetic relatedness between two individuals, e.g. brother-sister (0.5), 
parent-child (0.5), grandparent-grandchild (0.25), cousin-cousin (0.125), or even more exotic relations
like "dad's second cousin" (0.015625).

Parses common English phrases.  Draws [Graphviz][7] graphs.
Calculates both the degree of relatedness and the relatedness coefficient, as described 
in [this Wikipedia page][2].

Inspired by a dude on a message board wondering if it was cool for 
him to be dating his dad's second cousin.  (Answer: it's probably okay.)

Demo
-----------

A [live version of the Grails frontend app][3] is available to demonstrate the functionality.

Download
----------

Get the latest JAR [here][6]. 

Usage
----------

Calculate the relatedness coefficient between a brother and sister:


```java
RelatednessCalculator.calculate(BasicRelation.Sibling).getCoefficient(); // returns 0.5
```

Calculate the degree of relation instead:

```java
RelatednessCalculator.calculate(BasicRelation.Sibling).getAverageDegree(); // returns 2.0
```

Calculate the relatedness coefficient for a more complex relation, if you know the common ancestor(s):

```java
// overhead on a message board: "her dad is my grandma's cousin"
// i.e. they share two ancestors - his 2 great-great-grandparents are her 2 great-grandparents
Relation relation = new Relation(new CommonAncestor(4, 3), new CommonAncestor(4, 3));

RelatednessCalculator.calculate(relation).getCoefficient(); // returns 0.015625
```

Calculate the same relation using plain English:

```java


RelatednessCalculator.calculate(RelativeNameParser.parse(
        "grandma's cousin's daughter").getRelation()).getCoefficient(); // returns 0.015625
        
RelatednessCalculator.calculate(RelativeNameParser.parse(
        "dad's second cousin").getRelation()).getCoefficient(); //returns 0.015625
```

Draw a pretty graph for Graphviz:

```java
// returns a Graphviz graph that looks exactly like the picture in this README above
RelativeNameParser.parse("second cousin", true).getGraph().drawGraph();
```

You can also see the unit tests for other ideas about how to use the code.

Frontend code
--------------

This code is a Java backend to the [Relatedness Calculator Interface][4] Grails app.

Maven usage
-----------

Download the code, build, and install it:

```
git clone https://github.com/nolanlawson/RelatednessCalculator
cd RelatednessCalculator
mvn install
```

Then just add the following dependency to your ```pom.xml```:

```xml
<dependencies>
  ...
  <dependency>
    <groupId>com.nolanlawson.relatedness</groupId>
  	<artifactId>relatedness-calculator</artifactId>
  	<version>1.0</version>
  </dependency>
  ...
</dependencies>
```

Credits
-----------
Thanks to Richard Dawkins for his easy-to-understand explanation of how to calculate relatedness
in _The Selfish Gene_.


[1]: http://sam.zoy.org/wtfpl/
[2]: http://en.wikipedia.org/wiki/Coefficient_of_relationship
[3]: http://apps.nolanlawson.com/relatedness-calculator/
[4]: https://github.com/nolanlawson/RelatednessCalculatorInterface
[5]: http://nolanwlawson.files.wordpress.com/2011/04/relatedness_calculator_version_2.png?w=600
[6]: http://nolanlawson.s3.amazonaws.com/dist/com.nolanlawson.relatedness/release/1.0/relatedness-calculator-1.0.jar
[7]: http://www.graphviz.org/
