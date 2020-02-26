package io.tava.lang;

import io.tava.function.Function1;

public class Tuple2<T1, T2> implements Tuple {

	private final T1 value1;

	private final T2 value2;

	public Tuple2(T1 value1, T2 value2) {
		this.value1 = value1;
		this.value2 = value2;
	}

	public T1 getValue1() {
		return this.value1;
	}

	public T2 getValue2() {
		return this.value2;
	}

	public <R> R map(Function1<Tuple2<T1, T2>, R> map) {
		return map.apply(this);
	}

	public int hashCode() {
		return this.value1.hashCode() ^ this.value2.hashCode();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Tuple2)) {
			return false;
		}
		Tuple2 tuple2 = (Tuple2) obj;
		if (this == tuple2) {
			return true;
		}
		if (!tuple2.getValue1().equals(this.getValue1())) {
			return false;
		}
		return tuple2.getValue2().equals(this.getValue2());
	}

}