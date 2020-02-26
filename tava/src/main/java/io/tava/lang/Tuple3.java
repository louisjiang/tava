package io.tava.lang;

import io.tava.function.Function1;

public class Tuple3<T1, T2, T3> implements Tuple {

	private final T1 value1;

	private final T2 value2;

	private final T3 value3;

	public Tuple3(T1 value1, T2 value2, T3 value3) {
		this.value1 = value1;
		this.value2 = value2;
		this.value3 = value3;
	}

	public T1 getValue1() {
		return this.value1;
	}

	public T2 getValue2() {
		return this.value2;
	}

	public T3 getValue3() {
		return this.value3;
	}

	public <R> R map(Function1<Tuple3<T1, T2, T3>, R> map) {
		return map.apply(this);
	}

	public int hashCode() {
		return this.value1.hashCode() ^ this.value2.hashCode() ^ this.value3.hashCode();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Tuple3)) {
			return false;
		}
		Tuple3 tuple3 = (Tuple3) obj;
		if (this == tuple3) {
			return true;
		}
		if (!tuple3.getValue1().equals(this.getValue1())) {
			return false;
		}
		if (!tuple3.getValue2().equals(this.getValue2())) {
			return false;
		}
		return tuple3.getValue3().equals(this.getValue3());
	}

}