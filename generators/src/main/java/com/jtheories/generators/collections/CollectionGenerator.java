package com.jtheories.generators.collections;

import com.jtheories.core.generator.Generators;
import com.jtheories.core.generator.processor.Generator;
import com.jtheories.core.random.SourceOfRandom;
import java.beans.beancontext.BeanContextServicesSupport;
import java.beans.beancontext.BeanContextSupport;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Generator
public interface CollectionGenerator<T> {
	default Collection<T> generate(Class<T> type, Class<T>... annotations) {
		SourceOfRandom random = Generators.getGenerator(SourceOfRandom.class).generate();
		Supplier<Collection<T>> c = random.choice(
			ArrayDeque::new,
			ArrayList::new,
			BeanContextServicesSupport::new,
			BeanContextSupport::new,
			ConcurrentLinkedDeque::new,
			ConcurrentLinkedQueue::new,
			CopyOnWriteArrayList::new,
			CopyOnWriteArraySet::new,
			HashSet::new,
			LinkedBlockingDeque::new,
			LinkedBlockingQueue::new,
			LinkedHashSet::new,
			LinkedList::new,
			LinkedTransferQueue::new,
			Stack::new,
			Vector::new
		);
		return IntStream
			.range(0, random.getRandom().nextInt(99) + 1)
			.mapToObj(operand -> Generators.gen(type, annotations))
			.collect(Collectors.toCollection(c));
	}
}
