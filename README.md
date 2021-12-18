# JTestGenerator

## Targets

-
Generate
JTest
based
on
Prime-paths.
	-
	Expected
	output
-
Support
loops.
-
Support
tests
on
composite
types.

## Conceived Layers

This
model
is
outdated.

1.
Load
Java
source
code,
and
parse
it
into
AST.
2.
Analyze
AST,
and
generate
CFG.
3.
Find
prime
paths
using
CFG,
collecting
the
constraints
and
expected
result
on
each
path.
4.
Solve
the
constraints,
getting
a
set
of
inputs.
Combine
it
with
the
expected
result
to
produce
test
cases.
5.
Generate
JUnit
code
using
the
test
cases.
The
JUnit
code
contains
no
extra
spaces
so
that
we
can
check
it
correctness.
6.
Format
the
code
to
be
human-readable.

## What We've Done

-
From
Soot
Unit
Graph,
generate
a
set
P
of
prime
paths
of
a
method.
-
Extend
P
into
a
set
C
of
complete
paths,
i.e.
from
entrance
points
to
end
points.
-
Collect
constraints
on
the
complete
path.

## Reference

### Prime Paths

[This tutorial](Prime-Path-Coverage_compressed.pdf)
shows
how
to
use
a
brutal
method
to
find
simple
paths.

### Soot

- [Soot API Document](https://www.sable.mcgill.ca/soot/doc/)
- [Soot Command-line Options](https://soot-build.cs.uni-paderborn.de/public/origin/develop/soot/soot-develop/options/soot_options.htm)
  ,
  Corresponds
  to
  the `Options`
  class.