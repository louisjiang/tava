package io.tava.lang;

import io.tava.function.Function1;

public class Tuple1<T1> implements Tuple {

	private final T1 value1;

	public Tuple1(T1 value1) {
		this.value1 = value1;
	}

	public T1 getValue1() {
		return this.value1;
	}

	public <R> R map(Function1<Tuple1<T1>, R> map) {
		return map.apply(this);
	}

	public int hashCode() {
		return this.value1.hashCode();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Tuple1)) {
			return false;
		}
		Tuple1 tuple1 = (Tuple1) obj;
		if (this == tuple1) {
			return true;
		}
		return tuple1.getValue1().equals(this.getValue1());
	}

}