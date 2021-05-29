package com.jtheories.junit;

import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

public class JTheoriesRunner implements InvocationInterceptor {

	@Override
	public void interceptTestMethod(
		Invocation<Void> invocation,
		ReflectiveInvocationContext<Method> invocationContext,
		ExtensionContext extensionContext
	) throws Throwable {
		System.out.println("Running test with JTheories");
		invocation.proceed();
		/*
		AtomicReference<Throwable> throwable = new AtomicReference<>();

		IntStream
			.range(0, 100)
			.forEach(
				i -> {
					try {
						invocation.proceed();
					} catch (Throwable t) {
						throwable.set(t);
					}
				}
			);

		Throwable t = throwable.get();
		if (t != null) {
			throw t;
		}
	}*/
	}
}
