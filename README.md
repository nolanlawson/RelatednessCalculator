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

Java library for calculating the relatedness between two individuals, e.g. brother-sister (0.5), 
parent-child (0.5), grandparent-grandchild (0.25), cousin-cousin (0.125), etc. Calculates 
the degree of relatedness and the relatedness coefficient, as described 
in [this Wikipedia page][2].

Inspired by a dude on a message board wondering if it was cool for 
him to be dating his dad's second cousin.  (Answer: it's probably okay.)

Demo
-----------

A [live version of the Grails frontend app][3] is available to demonstrate the functionality.

Frontend code
--------------

This code is a Java backend to the [Relatedness Calculator Interface][4] Grails app.

Download
----------

Get the latest JAR [here][6]. 

Maven usage
-----------

Download the code, build, and install it:

```
git clone https://github.com/nolanlawson/RelatednessCalculator
cd RelatednessCalculator
mvn install
```

Then just add the following to your pom.xml:

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

Usage
----------

Calculate the relatedness coefficient between a brother and sister:


```java
RelatednessCalculator.calculate(BasicRelation.Sibling).getCoefficient(); // returns 0.5
```

You can also see the unit tests to get an idea of how to use the code.

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
