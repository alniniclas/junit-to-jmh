# junit-to-jmh

A tool for generating JMH benchmarks from JUnit 4 unit tests.

A number of utility scripts as well as scripts and source code used for conducting experiments on
the tool can be found in the [junit-to-jmh-experiment](https://github.com/alniniclas/junit-to-jmh-experiment)
repo.

## Basic usage

To run the junit-to-jmh tool, run the following command in the `junit-to-jmh` project directory.

```
$ ./gradlew converter:run --args="-h"
```

When generating JMH benchmarks from JUnit tests, the junit-to-jmh has four required arguments:

1. The root directory of the source files for the unit tests.
2. The root directory of the compiled class files for the tests.
3. The root directory where the resulting benchmark source files should be generated.
4. A list of fully-qulified test class names to generate benchmarks from.

Assuming we have a gradle project named `gradle-example` using JUnit 4 and the
[JMH Gradle Plugin](https://github.com/melix/jmh-gradle-plugin), and wish to generate benchmarks
from the unit tests in the class `com.example.ExampleTest` in the subproject `lib`, we can do so
using the following command:

```
$ ./gradlew converter:run --args="{path-to-project}/gradle-example/lib/src/test/java/ {path-to-project}/gradle-example/lib/build/classes/java/test/ {path-to-project}/gradle-example/lib/src/jmh/java/ com.example.ExampleTest"
```

Here, `{path-to-project}` is the directory that `gradle-example` is located in. To generate
benchmarks from larger numbers of test classes, the list of classes can also be read from a file as
follows:

```
$ ./gradlew converter:run --args="{path-to-project}/gradle-example/lib/src/test/java/ {path-to-project}/gradle-example/lib/build/classes/java/test/ {path-to-project}/gradle-example/lib/src/jmh/java/ --class-names-file={path-to-classes-file}/test-classes.txt"
```

Here, `test-classes.txt` is a plaintext file containing the fully-qualified names of the classes to
generate benchmarks from on separate lines.

If the command completes successfully, junit-to-jmh should have generated copies of all classes in
`{path-to-project}/gradle-example/lib/src/test/java/` and placed them in the
`{path-to-project}/gradle-example/lib/src/jmh/java/` directory. Additionally, in the generated copy
of `com.example.ExampleTest`, a nested class named `_Benchmark` should have been added, containing
benchmark methods executing each of the unit tests in `ExampleTest`.

The dependencies of the generated classes should be the same as the dependencies of the original
tests, with an additional dependency on JMH (provided automatically by the JMH Gradle Plugin).

Although generating benchmarks for Maven projects has not yet been tested, the process for doing so
should likely be similar as long as the correct source code and class file directories are provided
to the tool.

## Longer example

Though the previous section covered the basics of how to use the tool, a longer, more thorough
example of how to generate benchmarks for an actual project may perhaps also be helpful. The
initial version of the example was written to stand on its own, so please forgive any reduncancies
with the previous section.

The following example assumes you have a working directory named `/tmp/ju2jmh-example/`, where this
project and
[junit-to-jmh-experiment](https://github.com/alniniclas/junit-to-jmh-experiment) have both been
checked out to `/tmp/ju2jmh-example/junit-to-jmh` and `/tmp/ju2jmh-example/junit-to-jmh-experiment`,
respectively, though feel free to use a different directory structure.

The project we'll be trying to generate benchmarks for lives in
`/tmp/ju2jmh-example/junit-to-jmh-experiment/slowdown-detection/`. This is a gradle project with
three subprojects: `main`, `ju2jmh`, and `ju4runner`, though for the purposes of this example, we
only care about `main` and `ju2jmh`. The `ju2jmh` subproject already contains generated benchmark
classes, which we'll be deleting and trying to generate again later, but for now let's just try
building the project to make sure that everything works as intended:

```
$ cd /tmp/ju2jmh-example/junit-to-jmh-experiment/slowdown-detection/
$ ./gradlew build
```

If the build completes successfully, we're good to go! Delete all source files in the jmh/java
directory of the ju2jmh subproject so that we can generate them again using the tool:

```
$ rm -r ju2jmh/src/jmh/java/*
```

Optionally, clean the project and build it again to make sure it still works:

```
$ ./gradlew clean
$ ./gradlew build
```

Now, it's time to generate some benchmarks! The benchmark generation tool can be run as follows:

```
$ cd /tmp/ju2jmh-example/junit-to-jmh/
$ ./gradlew converter:run
```

This will produce an error, because multiple required arguments are missing. To pass arguments to
the tool through Gradle, use `--args`, e.g.:

```
$ ./gradlew converter:run --args="-h"
```

The benchmark generation tool has four required inputs:

* The path to the source code directory of the test classes to generate benchmarks from. In our
  example, the source code of the tests lives in 
  `/tmp/ju2jmh-example/junit-to-jmh-experiment/slowdown-detection/main/src/test/java/`.
* The path to the compiled class files of the same test classes. After building the
  `junit-to-jmh-experiment/slowdown-detection` project, these should live in
  `/tmp/ju2jmh-example/junit-to-jmh-experiment/slowdown-detection/main/build/classes/java/test/`.
* The path to the directory that the benchmarks should be generated in. In our example, this is
  `/tmp/ju2jmh-example/junit-to-jmh-experiment/slowdown-detection/ju2jmh/src/jmh/java/`.
* A list of which test classes to generate benchmarks from, either specified separately as arguments
  on the command line, or on separate lines in a plaintext file specified using the
  `--class-names-file` argument. When producing benchmarks from just a few test classes, this can be
  specified manually, but when working with a larger number of tests, it is easier to just run the
  tests and use a script to extract the names of the test classes from the XML reports produced by
  the test run. At the moment, automatically producing benchmarks for *all* tests is not supported
  by the tool.

To produce the list of tests to generate benchmarks from, we'll use the `list-tests.py` script that
lives in `/tmp/ju2jmh-example/junit-to-jmh-experiment/scripts/gradle/`. Before running the script,
make sure that all the tests have been run, so that a test report is available:

```
$ cd /tmp/ju2jmh-example/junit-to-jmh-experiment/slowdown-detection/
$ ./gradlew test
```

Now, XML reports should be available in
`/tmp/ju2jmh-example/junit-to-jmh-experiment/slowdown-detection/main/build/test-results/test/`.
From these, we can now generate a list of the test classes as follows:

```
$ cd /tmp/ju2jmh-example
$ python3 junit-to-jmh-experiment/scripts/gradle/list_tests.py --classes-only --plaintext-output junit-to-jmh-experiment/slowdown-detection/main/build/test-results/test/ test-classes.txt
```

This will generate a file called `test-classes.txt` in `/tmp/ju2jmh-example/`, containing the fully
qualified names of each unit test class on separate lines. This file is the last piece of input we
need for running the benchmark generation tool.

Change directories again to that of the benchmark generation tool, in order to be able to run it:

```
$ cd /tmp/ju2jmh-example/junit-to-jmh/
```

Now, to generate the junit-to-jmh benchmarks, run:

```
$ ./gradlew converter:run --args="/tmp/ju2jmh-example/junit-to-jmh-experiment/slowdown-detection/main/src/test/java/ /tmp/ju2jmh-example/junit-to-jmh-experiment/slowdown-detection/main/build/classes/java/test/ /tmp/ju2jmh-example/junit-to-jmh-experiment/slowdown-detection/ju2jmh/src/jmh/java/ --class-names-file=/tmp/ju2jmh-example/test-classes.txt"
```

If you've done everything correctly, benchmarks should now have been generated for all unit tests in
the project. To build the generated benchmarks, run the following:

```
$ cd /tmp/ju2jmh-example/junit-to-jmh-experiment/slowdown-detection/
$ ./gradlew jmhJar
```

This should produce JMH JAR files for the generated benchmarks in the `ju2jmh` subproject (as well
as for the manually written benchmarks present in the `main` subproject). The generated JMH JAR
should be located at
`/tmp/ju2jmh-example/junit-to-jmh-experiment/slowdown-detection/ju2jmh/build/libs/ju2jmh-jmh.jar`.
To list the available benchmarks, run:

```
$ java -jar /tmp/ju2jmh-example/junit-to-jmh-experiment/slowdown-detection/ju2jmh/build/libs/ju2jmh-jmh.jar -l
```

To make sure that the benchmarks are able to run without errors, you can do a quick run of all of
them e.g. as follows:

```
$ java -jar /tmp/ju2jmh-example/junit-to-jmh-experiment/slowdown-detection/ju2jmh/build/libs/ju2jmh-jmh.jar -f 1 -wi 0 -i 1 -r 100ms -foe true
```

You can also run the benchmarks properly with any standard JMH arguments you feel like.
