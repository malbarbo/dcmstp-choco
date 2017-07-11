/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.chocosolver.graphsolver.GraphModel;
import org.chocosolver.graphsolver.search.strategy.GraphStrategies;
import org.chocosolver.graphsolver.variables.IUndirectedGraphVar;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.objective.ObjectiveStrategy;
import org.chocosolver.solver.objective.OptimizationPolicy;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.io.*;

/**
 * Solves the Degree Constrained Minimum Spanning Tree Problem
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class DCMST {

	//***********************************************************************************
	// BENCHMARK
	//***********************************************************************************

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.printf(
				"Invalid usage. One argument is required but %d was given\n",
				args.length
			);
			System.exit(1);
		}
		new DCMST(args[0]).solve();
	}

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	// input
	private int n;
	private int[] dMax;
	private int[][] dist;
	private int lb, ub;
	private String instance;

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

	public DCMST(String path) {
		File file = new File(path);
		parse(file);
		instance = file.getName();
	}

	//***********************************************************************************
	// MODEL
	//***********************************************************************************

	public void solve() {
		GraphModel model = new GraphModel();
		IntVar totalCost = model.intVar("obj", lb, ub, true);
		// graph var domain
		UndirectedGraph GLB = new UndirectedGraph(model,n,SetType.LINKED_LIST,true);
		UndirectedGraph GUB = new UndirectedGraph(model,n,SetType.BIPARTITESET,true);
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				if (dist[i][j] != -1 && !(dMax[i] == 1 && dMax[j] == 1)) {
					GUB.addEdge(i, j); // possible edge
				}
			}
		}
		IUndirectedGraphVar graph = model.graphVar("G", GLB, GUB);
		IntVar[]degrees = model.degrees(graph);
		for (int i = 0; i < n; i++) {
			model.arithm(degrees[i], "<=", dMax[i]).post();
		}

		// degree constrained-minimum spanning tree constraint
		model.dcmst(graph,degrees,totalCost,dist,2).post();

		final GraphStrategies mainSearch = new GraphStrategies(graph, dist);
		// find the first solution by selecting cheap edges
		mainSearch.configure(GraphStrategies.MIN_COST, true);
		Solver s = model.getSolver();
		// then select the most expensive ones (fail first principle, with last conflict)
		s.plugMonitor((IMonitorSolution) () -> {
            mainSearch.useLastConflict();
            mainSearch.configure(GraphStrategies.MIN_P_DEGREE, true);
            System.out.println("Solution: " + totalCost);
        });
		// bottom-up optimization : find a first solution then reach the global minimum from below
		s.setSearch(new ObjectiveStrategy(totalCost, OptimizationPolicy.BOTTOM_UP), mainSearch);
		s.limitSolution(2); // therefore there is at most two solutions
		long T4HOURS = 4 * 60 * 60 * 1000;
		long TIMELIMIT = T4HOURS;
		s.limitTime(TIMELIMIT);

		// find optimum
		model.setObjective(Model.MINIMIZE,totalCost);
		while (s.solve()){
			System.out.println("Solution: " + totalCost);
		}

		if (s.getSolutionCount() == 0 && s.getTimeCount() < TIMELIMIT/1000) {
			throw new UnsupportedOperationException("Provided instances are feasible!");
		}
		System.out.println("Best: " + s.getBestSolutionValue());
		System.out.println("Time: " + s.getTimeCount());
	}

	//***********************************************************************************
	// PARSING
	//***********************************************************************************

	public boolean parse(File file) {
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file));
			String line = buf.readLine();
			String[] numbers = line.split(" ");
			n = Integer.parseInt(numbers[0]);
			int m = Integer.parseInt(numbers[1]);
			dist = new int[n][n];
			dMax = new int[n];
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					dist[i][j] = -1;
				}
			}
			int from, to, cost;
			int min = 1000000;
			int max = 0;
			// parse edges
			for (int i = 0; i < m; i++) {
				line = buf.readLine();
				numbers = line.split(" ");
				from = Integer.parseInt(numbers[0]) - 1;
				to = Integer.parseInt(numbers[1]) - 1;
				cost = Integer.parseInt(numbers[2]);
				min = Math.min(min, cost);
				max = Math.max(max, cost);
				if (dist[from][to] != -1 || dist[to][from] != -1) {
					throw new UnsupportedOperationException();
				}
				dist[from][to] = dist[to][from] = cost;
			}
			// parse degrees
			for (int i = 0; i < n; i++) {
				line = buf.readLine();
				numbers = line.split(" ");
				if (Integer.parseInt(numbers[0]) != i + 1) {
					throw new UnsupportedOperationException();
				}
				dMax[i] = Integer.parseInt(numbers[1]);
			}
			lb = (n - 1) * min;
			ub = (n - 1) * max;
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		throw new UnsupportedOperationException();
	}
}
