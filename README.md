DCMSTP ([Degree constraint minimum spanning tree
problem](https://en.wikipedia.org/wiki/Degree-constrained_spanning_tree))
solver extract from [choco-graph](https://github.com/chocoteam/choco-graph)
samples. The solver is described in
[this](http://www.lsis.org/jfpc-jiaf2013/jfpc/articles/papier_31.pdf) paper.


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
