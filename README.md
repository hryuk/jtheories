# JTheories
A property based testing library using java anotations and automatic dependency resolution. Not production ready, a lot of things still to do.

## Usage example
For a given class:

```java
package com.jtheories.examples;

import java.util.UUID;

public class Product {

	private final UUID id;

	private final String name;

	private final Long price;

	public Product(UUID id, String name, Long price) {
		this.id = id;
		this.name = name;
		this.price = price;
	}

	public UUID getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public Long getPrice() {
		return this.price;
	}

	@Override
	public String toString() {
		return (
			"Product{" +
			"id=" +
			this.id +
			", name='" +
			this.name +
			'\'' +
			", price=" +
			this.price +
			'}'
		);
	}
}

```
You can define a generator:

```java
package com.jtheories.examples;

import com.jtheories.core.generator.meta.GeneratorAnnotations;
import com.jtheories.core.generator.processor.Generator;
import com.jtheories.generators.number.NotMultipleOf;
import com.jtheories.generators.number.Positive;
import java.util.UUID;

@Generator
public interface ProductGenerator {
	default Product generate(
		UUID id,
		String name,
		@Positive @NotMultipleOf(10) Long price
	) {
		return new Product(id, name, price);
	}

	@Free
	default Product free(Product product) {
		return new Product(product.getId(), product.getName(), 0L);
	}

	@Price
	default Product price(Product product, GeneratorAnnotations annotations) {
		return new Product(product.getId(), product.getName(), annotations.get(Price.class));
	}

	@Name
	default Product name(Product product, GeneratorAnnotations annotations) {
		return new Product(product.getId(), annotations.get(Name.class), product.getPrice());
	}
}

```

And then the generator will be used to generate clases matching de requested data during testing:

```java

	@Test
	void productListIsFree() {
		theory()
			.<Collection<@Free Product>>forAll()
			.check(
				products -> {
					Order order = new Order(products);
					Assertions.assertEquals(0L, order.getTotalPrice());
				}
			);
	}
```

[more usage examples](https://github.com/hryuk/jtheories/tree/develop/examples/src/test/java/com/jtheories/examples)

## License

The project has been released under the [GNU LGPLv3](https://github.com/hryuk/jtheories/blob/develop/LICENSE).
