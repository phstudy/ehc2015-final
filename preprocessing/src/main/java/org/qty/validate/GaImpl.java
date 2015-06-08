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
import com.google.common.collect.Sets;

public class GaImpl {

    private static final int MAGIC = 16;
    public static final int MAX_AMOUNT = 10000 * 100 * 4;
    static Random random = new Random();

    // parameters for the GA
    private static final int POPULATION_SIZE = 50;
    private static final int NUM_GENERATIONS = 200000;
    private static final double ELITISM_RATE = 0.5;
    private static final double CROSSOVER_RATE = 1;
    private static final double MUTATION_RATE = 0.5;
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

    private int knownInTop20(ItemCounter<String> itemCounter) {
        Set<String> predict = Sets.newHashSet();
        List<Entry<String, AtomicInteger>> list = itemCounter.getTopN(20);
        for (Entry<String, AtomicInteger> item : list) {
            predict.add(item.getKey());
        }
        //        System.out.println("top" + 20 + " => " + Sets.intersection(predict, TestAnswer.ANSWER_PIDS).size());

        Entry<String, AtomicInteger> itemIn20th = list.get(19);
        int item20amount = itemIn20th.getValue().intValue() * priceMap.get(itemIn20th.getKey());

        int itemInTop20 = Sets.intersection(predict, TestAnswer.ANSWER_PIDS).size();
        if (itemInTop20 == MAGIC && item20amount < 40 * 10000) {
            // 未滿 40 萬，故意不滿足
            return item20amount - 1;
        }
        return itemInTop20;
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
                        int inTop20 = knownInTop20(((BuyCountChromosome) current.getFittestChromosome()).itemCounter);
                        System.out.println(progress + " => " + inTop20 + ", " + current.getFittestChromosome());
                        if (inTop20 == MAGIC) {
                            System.err.println("early break");
                            break;
                        }
                        if (current.getFittestChromosome().getFitness() < 1) {
                            throw new IllegalStateException("give up: " + current.getFittestChromosome().getFitness());
                        }
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
            int fitness = maxValue - getCostWithoutExecption(rep);

            int bouns = 0;
            for (int i = 0; i < repList.size() - 1; i++) {
                int v1 = repList.get(i);
                int v2 = repList.get(i + 1);

                String k1 = keyOrders.get(i);
                String k2 = keyOrders.get(i + 1);
                if (pidWeight.get(k1) > pidWeight.get(k2) && v1 > v2) {
                    bouns++;
                }
            }

//            if (itemCounter != null) {
//                int inTop20 = knownInTop20(itemCounter);
//                if (inTop20 >= MAGIC) {
//                    fitness += 100;
//                }
//
//                List<Entry<String, AtomicInteger>> top20List = itemCounter.getTopN(20);
//                Entry<String, AtomicInteger> itemIn1st = top20List.get(1);
//                Entry<String, AtomicInteger> itemIn20th = top20List.get(19);
//
//                int item1amount = itemIn1st.getValue().intValue() * priceMap.get(itemIn1st.getKey());
//                int item20amount = itemIn20th.getValue().intValue() * priceMap.get(itemIn20th.getKey());
//                // top1 超過 400 萬就丟棄
//                if (item1amount > MAX_AMOUNT) {
//                    return 0;
//                }
//
//                final int meet20th = 45 * 10000;
//                int item20Dist = Math.abs(item20amount - meet20th);
//                fitness += (meet20th - item20Dist);
//            }

            return bouns + fitness;
        }

        @Override
        public AbstractListChromosome<Integer> newFixedLengthChromosome(List<Integer> representation) {
            return new BuyCountChromosome(representation);
        }

        private int knownInTop20(ItemCounter<String> itemCounter) {
            Set<String> predict = Sets.newHashSet();
            for (Entry<String, AtomicInteger> item : itemCounter.getTopN(20)) {
                predict.add(item.getKey());
            }
            return Sets.intersection(predict, TestAnswer.ANSWER_PIDS).size();
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
                    value = Math.abs(value);
                }
            }

            if (f.itemCounter != null) {
                List<Entry<String, AtomicInteger>> item = f.itemCounter.getTopN(1);
                if (item.get(0).getValue().intValue() * priceMap.get(item.get(0).getKey()) > MAX_AMOUNT) {
                    item.get(0).getValue().set(1);
                }
            }

            newList.set(index, value);
            return f.newFixedLengthChromosome(newList);
        }
    }

}
