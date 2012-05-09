Relatedness Calculator
=========================

Simple tool to calculate the relatedness between two individuals, e.g. brother-sister (0.5), 
parent-child (0.5), grandparent-grandchild (0.25), cousin-cousin (0.125), etc. Calculates 
the degree of relatedness and the relatedness coefficient, as described 
in [this Wikipedia page][2].

Inspired by a dude on a message board wondering if it was cool for 
him to be dating his dad's second cousin.

Installation
----------

Download the code and install it:

```
git clone https://github.com/nolanlawson/RelatednessCalculator
cd RelatednessCalculator
mvn install
```

Then add the following to your pom.xml:

```xml
<dependencies>
  ...
  <dependency>
  	<groupId>com.nolanlawson.relatedness</groupId>
  	<artifactId>relatedness-calculator</artifactId>
  	<version>0.0.1-SNAPSHOT</version>
  </dependency>
  ...
</dependencies>
```

Usage
----------

See the unit tests to get an idea of how to use the code.

Developer
-----------

Nolan Lawson

Credits
-----------
Thanks to Richard Dawkins for his easy-to-understand explanation of how to calculate relatedness
in _The Selfish Gene_.

License
-----------

[WTFPL][1].

[1]: http://sam.zoy.org/wtfpl/
[2]: http://en.wikipedia.org/wiki/Coefficient_of_relationship