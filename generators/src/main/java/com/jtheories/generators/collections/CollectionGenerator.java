package com.jtheories.generators.collections;

import com.jtheories.core.generator.Generators;
import com.jtheories.core.generator.processor.Generator;
import com.jtheories.core.random.SourceOfRandom;

import java.beans.beancontext.BeanContextServicesSupport;
import java.beans.beancontext.BeanContextSupport;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Generator
public interface CollectionGenerator<T> {

  default Collection<T> generate(Class<T> type, Class<T>... annotations) {
    SourceOfRandom random = Generators.getGenerator(SourceOfRandom.class).generate();
    Supplier<Collection<T>> c =
        random.choice(
            ArrayDeque::new,
            ArrayList::new,
            BeanContextServicesSupport::new,
            BeanContextSupport::new,
            ConcurrentLinkedDeque::new,
            ConcurrentLinkedQueue::new,
            ConcurrentSkipListSet::new,
            CopyOnWriteArrayList::new,
            CopyOnWriteArraySet::new,
            HashSet::new,
            LinkedBlockingDeque::new,
            LinkedBlockingQueue::new,
            LinkedHashSet::new,
            LinkedList::new,
            LinkedTransferQueue::new,
            PriorityBlockingQueue::new,
            PriorityQueue::new,
            Stack::new,
            SynchronousQueue::new,
            TreeSet::new,
            Vector::new);
    return IntStream.range(0, random.getRandom().nextInt(99) + 1)
        .mapToObj(operand -> Generators.gen(type, annotations))
        .collect(Collectors.toCollection(c));
  }
}
