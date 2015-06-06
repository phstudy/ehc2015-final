package org.qty.validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.genetics.AbstractListChromosome;
import org.apache.commons.math3.genetics.BinaryChromosome;
import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.ElitisticListPopulation;
import org.apache.commons.math3.genetics.FixedGenerationCount;
import org.apache.commons.math3.genetics.GeneticAlgorithm;
import org.apache.commons.math3.genetics.InvalidRepresentationException;
import org.apache.commons.math3.genetics.MutationPolicy;
import org.apache.commons.math3.genetics.OnePointCrossover;
import org.apache.commons.math3.genetics.Population;
import org.apache.commons.math3.genetics.RandomKey;
import org.apache.commons.math3.genetics.StoppingCondition;
import org.apache.commons.math3.genetics.TournamentSelection;
import org.qty.ItemCounter;

import com.google.common.collect.Lists;

public class GaImpl {

    static Random random = new Random();

    // parameters for the GA
    private static final int POPULATION_SIZE = 50;
    private static final int NUM_GENERATIONS = 20000;
    private static final double ELITISM_RATE = 0.2;
    private static final double CROSSOVER_RATE = 1;
    private static final double MUTATION_RATE = 0.2;
    private static final int TOURNAMENT_ARITY = 5;

    static Map<String, Integer> priceMap;
    static Set<String> answerSet;
    static Map<String, Float> pidWeight;
    static List<String> keyOrders;

    public GaImpl(Map<String, Integer> priceMap, Set<String> answerSet, Map<String, Float> pidWeight) {
        this.priceMap = priceMap;
        this.answerSet = answerSet;
        this.pidWeight = pidWeight;
        this.keyOrders = new ArrayList<String>(pidWeight.keySet());
    }

    public BuyCountChromosome evolve() {
        GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(new OnePointCrossover<Integer>(), CROSSOVER_RATE,
                new BuyCountMutation(), MUTATION_RATE, new TournamentSelection(TOURNAMENT_ARITY)) {

            public Population evolve(final Population initial, final StoppingCondition condition) {
                Population current = initial;
                int progress = 0;
                while (!condition.isSatisfied(current)) {
                    current = nextGeneration(current);
                    if (progress++ % 50 == 0) {
                        System.out.println(current.getFittestChromosome());
                    }
                }
                return current;
            }
        };

        // initial population
        Population initial = getInitialPopulation();

        // stopping condition
        StoppingCondition stopCond = new FixedGenerationCount(NUM_GENERATIONS);

        // run the algorithm
        Population finalPopulation = geneticAlgorithm.evolve(initial, stopCond);
        // best chromosome from the final population
        Chromosome bestFinal = finalPopulation.getFittestChromosome();
        return (BuyCountChromosome) bestFinal;
    }

    private Population getInitialPopulation() {
        List<Chromosome> popList = new LinkedList<Chromosome>();

        for (int i = 0; i < POPULATION_SIZE; i++) {
            BuyCountChromosome chromosome = new BuyCountChromosome(generateRandomPopulation());
            popList.add(chromosome);
        }

        return new ElitisticListPopulation(popList, popList.size(), ELITISM_RATE);
    }

    public ArrayList<Integer> generateRandomPopulation() {
        ArrayList<Integer> ooo = new ArrayList<Integer>();
        int[] p = new int[priceMap.keySet().size()];
        for (int i = 0; i < p.length; i++) {
            //            p[i] = Math.abs(random.nextInt(10));
            ooo.add(Math.abs(random.nextInt(10)));
        }
        return ooo;
    }

    public static void main(String[] args) {

    }

    static class BuyCountChromosome extends BinaryChromosome {

        ItemCounter<String> itemCounter;
        List<String> ranking;

        public BuyCountChromosome(List<Integer> representation) throws InvalidRepresentationException {
            super(representation);
        }

        @Override
        protected void checkValidity(List<Integer> chromosomeRepresentation) throws InvalidRepresentationException {
        }

        @Override
        public List<Integer> getRepresentation() {
            return super.getRepresentation();
        }

        @Override
        public double fitness() {
            List<Integer> repList = getRepresentation();
            int[] rep = new int[repList.size()];
            for (int i = 0; i < rep.length; i++) {
                rep[i] = repList.get(i);
            }

            int maxValue = repList.size() * repList.size();
            return maxValue - getCostWithoutExecption(rep);
        }

        @Override
        public AbstractListChromosome<Integer> newFixedLengthChromosome(List<Integer> representation) {
            return new BuyCountChromosome(representation);
        }

        protected int getCostWithoutExecption(int[] buyCounts) {
            int index = 0;
            ItemCounter<String> c = new ItemCounter<String>();
            for (Entry<String, Integer> priceEntry : priceMap.entrySet()) {
                int count = buyCounts[index++];
                c.count(priceEntry.getKey(), count * priceEntry.getValue());
            }

            List<String> ranking = toOrderedKeys(c);
            this.ranking = ranking;
            this.itemCounter = c;

            int cost = 0;
            for (String s : answerSet) {
                int offset = ranking.indexOf(s);
                if (offset == -1) {
                    cost += 10000;
                } else if (offset <= 20) {
                    cost += 0;
                } else {
                    cost += (offset - 20);
                }
            }
            return cost;
        }

        public List<String> toOrderedKeys(ItemCounter<String> c) {
            ArrayList<Entry<String, AtomicInteger>> list = Lists.newArrayList(c.entrySet());
            Collections.sort(list, new Comparator<Entry<String, AtomicInteger>>() {
                @Override
                public int compare(Entry<String, AtomicInteger> o1, Entry<String, AtomicInteger> o2) {
                    if (o2.getValue().intValue() == o1.getValue().intValue()) {
                        return 0;
                    }
                    return o2.getValue().intValue() > o1.getValue().intValue() ? 1 : -1;
                }
            });

            ArrayList<String> ss = new ArrayList<String>();
            for (Entry<String, AtomicInteger> e : list) {
                ss.add(e.getKey());
            }
            return ss;
        }
    }

    static class BuyCountMutation implements MutationPolicy {

        /**
         * {@inheritDoc}
         *
         * @throws MathIllegalArgumentException
         *             if <code>original</code> is not a {@link RandomKey}
         *             instance
         */
        public Chromosome mutate(final Chromosome original) throws MathIllegalArgumentException {

            BuyCountChromosome f = (BuyCountChromosome) original;
            ArrayList<Integer> newList = new ArrayList<Integer>(f.getRepresentation());
            int index = Math.abs(GeneticAlgorithm.getRandomGenerator().nextInt()) % newList.size();

            int value = newList.get(index);
            int delta = GeneticAlgorithm.getRandomGenerator().nextInt(10);
            boolean addition = GeneticAlgorithm.getRandomGenerator().nextBoolean();
            if (addition && value < 500) {
                value += delta;
            } else {
                value -= delta;
                if (value < 0) {
                    value = 0;
                }
            }
            newList.set(index, value);

            //
            if (index < newList.size() - 1) {
                // 透過 pid-weight 簡易比對，保持與 model 的一致
                int nextValue = newList.get(index + 1);
                String k1 = keyOrders.get(index);
                String k2 = keyOrders.get(index + 1);
                boolean consistence = pidWeight.get(k1) > pidWeight.get(k2) && value > nextValue;
                if (!consistence) {
                    newList.set(index, nextValue);
                    newList.set(index + 1, value);
                }
            }

            return f.newFixedLengthChromosome(newList);
        }
    }

}
