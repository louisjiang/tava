package io.tava.lang;

import io.tava.function.Function1;

public class Tuple4<T1, T2, T3, T4> implements Tuple {

	private final T1 value1;

	private final T2 value2;

	private final T3 value3;

	private final T4 value4;

	public Tuple4(T1 value1, T2 value2, T3 value3, T4 value4) {
		this.value1 = value1;
		this.value2 = value2;
		this.value3 = value3;
		this.value4 = value4;
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

	public T4 getValue4() {
		return this.value4;
	}

	public <R> R map(Function1<Tuple4<T1, T2, T3, T4>, R> map) {
		return map.apply(this);
	}

	public int hashCode() {
		return this.value1.hashCode() ^ this.value2.hashCode() ^ this.value3.hashCode() ^ this.value4.hashCode();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Tuple4)) {
			return false;
		}
		Tuple4 tuple4 = (Tuple4) obj;
		if (this == tuple4) {
			return true;
		}
		if (!tuple4.getValue1().equals(this.getValue1())) {
			return false;
		}
		if (!tuple4.getValue2().equals(this.getValue2())) {
			return false;
		}
		if (!tuple4.getValue3().equals(this.getValue3())) {
			return false;
		}
		return tuple4.getValue4().equals(this.getValue4());
	}

}