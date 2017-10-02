DCMSTP ([Degree constraint minimum spanning tree
problem](https://en.wikipedia.org/wiki/Degree-constrained_spanning_tree))
solver extract from [choco-graph](https://github.com/chocoteam/choco-graph)
samples. The solver is described in the paper [The salesman and the tree: the
importance of search in CP](https://doi.org/10.1007/s10601-014-9178-2).

This was extract from choco-graph to make it easy to use with
[dcmstp-instances](https://github.com/malbarbo/dcmstp-instances).

## Changes from the original code

- Input parse and output printing
- Time limit can be passed in the command line

## Build

```
mvn package
```

## Usage

```
java -jar target/dcmstp-choco-4.1.1-shaded.jar [time limit in seconds] input-file
```

## Input format

```
n m
m lines with format "vertex vertex cost"
n lines with format "vertex degree"
```

where `n` is the number of vertices and `m` i the number of edges.

Example:

```
4 6
1 2 20
1 3 10
1 4 30
2 3 15
2 4 18
3 4 17
1 2
2 2
3 3
4 2
```

## License

This software is licensed under BSD license.
