package com.example.dani.biketracker.controlModule;

public class RMPC {
	
	int Npasos;
	
	//Solo aguanta 5 pasos por ahora
	public RMPC(int Npasos) {
		this.Npasos = Npasos;
	}
	
	public double getControlAction(errorBuffer buffer, pairStatePackage paquete) {
		errorBuffer cloneBuffer = cloneBuffer(buffer);
		fuzzyInterval fuzzyInt = new fuzzyInterval();
		fuzzyInt.intervalosSim(cloneBuffer, Npasos);
		double out = runGA(50, 0.60, 0.40, (int)(0.05*50), fuzzyInt, Npasos, paquete, 2000);
		return out;
	}
	
	public double runGA(int populationSize, double mutationRate, double crossoverRate, int elitismCount, fuzzyInterval fuzzyInt, int Npasos, pairStatePackage paquete, int stopCondition) {
		
		GeneticAlgorithm ga = new GeneticAlgorithm(populationSize, mutationRate, crossoverRate, elitismCount, fuzzyInt);

		// Initialize population
		Population population = ga.initPopulation(2*Npasos);
		
		population = ga.restrictionImplementation(population, Npasos);

		// Evaluate population
		ga.evalPopulation(population, paquete, Npasos);

		// Keep track of current generation
		int generation = 1;

		/**
		 * Start the evolution loop
		 */
		int counter = 0;
		int toggle = 0;
		
		double tolerance = 0.01;
		double[] fitArray = new double[] {0, 0};
		double delta;
		
		while (counter < stopCondition) {			
			// Apply crossover
			population = ga.crossoverPopulation(population);

			// Apply mutation
			population = ga.mutatePopulation(population);
			
			// Apply inequality restrictions
			population = ga.restrictionImplementation(population, Npasos);

			// Evaluate population
			ga.evalPopulation(population, paquete, Npasos);
			
			//Stop condition
			if (toggle == 0) {
				fitArray[toggle] = population.getFittest(0).getFitness();
				delta = Math.abs(fitArray[0] - fitArray[1]);
				
				if (delta < tolerance) {counter += 1;}
				else {counter = 0;}
				
				toggle = 1;
				}
			else {
				fitArray[toggle] = population.getFittest(0).getFitness();
				delta = Math.abs(fitArray[0] - fitArray[1]);
				
				if (delta < tolerance) {counter += 1;}
				else {counter = 0;}
				
				toggle = 0;
				}

			// Increment the current generation
			generation++;
		}

		/*System.out.println("Found solution in " + generation + " generations");
		System.out.println("Best solution: " + population.getFittest(0).toString());
		System.out.println("Fitenss value: " + population.getFittest(0).getFitness());*/
		return population.getFittest(0).getGene(0);
		
	}
	
	public errorBuffer cloneBuffer(errorBuffer buffer) {
		
		errorBuffer clone = new errorBuffer(buffer.getBufferLength());
		
		for (int i = 0; i < buffer.getBufferLength(); i++) {
			clone.setError(i, buffer.getError(i)); 
		}
		
		return clone;
	}
	
	/*public static void main(String [] args)	{
		
		int Npasos = 5;
		RMPC rmpc = new RMPC(Npasos);
		
		pairStatePackage paquete = new pairStatePackage(0, 10, 4, 5, 5);
		errorBuffer buffer = new errorBuffer(new double[] {0.01, -0.3, 0.2, 0.21, 0.01});
		
		double u_k = rmpc.getControlAction(buffer, paquete);
		System.out.println("u(k) : " + u_k);
	
	}*/
	
}
