# JTestGenerator

## Layers

1. Load Java source code, and parse it into AST.
2. Analyze AST, and generate CFG.
3. Find prime paths using CFG, collecting the constraints and expected result on each path.
4. Solve the constraints, getting a set of inputs. Combine it with the expected result to produce test cases.
5. Generate JUnit code using the test cases. The JUnit code contains no extra spaces so that we can check it correctness.
6. Format the code to be human-readable.

## What Do We Need To Do Now



## What We've Done

- From Soot Unit Graph, generate a set P of prime paths of a method.
- Extend P into a set C of complete paths, i.e. from entrance points to end points.
- Collect constraints on the complete path.