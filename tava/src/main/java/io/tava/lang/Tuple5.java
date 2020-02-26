package io.tava.lang;

import io.tava.function.Function1;

public class Tuple5<T1, T2, T3, T4, T5> implements Tuple {

	private final T1 value1;

	private final T2 value2;

	private final T3 value3;

	private final T4 value4;

	private final T5 value5;

	public Tuple5(T1 value1, T2 value2, T3 value3, T4 value4, T5 value5) {
		this.value1 = value1;
		this.value2 = value2;
		this.value3 = value3;
		this.value4 = value4;
		this.value5 = value5;
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

	public T5 getValue5() {
		return this.value5;
	}

	public <R> R map(Function1<Tuple5<T1, T2, T3, T4, T5>, R> map) {
		return map.apply(this);
	}

	public int hashCode() {
		return this.value1.hashCode() ^ this.value2.hashCode() ^ this.value3.hashCode() ^ this.value4.hashCode() ^ this.value5.hashCode();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Tuple5)) {
			return false;
		}
		Tuple5 tuple5 = (Tuple5) obj;
		if (this == tuple5) {
			return true;
		}
		if (!tuple5.getValue1().equals(this.getValue1())) {
			return false;
		}
		if (!tuple5.getValue2().equals(this.getValue2())) {
			return false;
		}
		if (!tuple5.getValue3().equals(this.getValue3())) {
			return false;
		}
		if (!tuple5.getValue4().equals(this.getValue4())) {
			return false;
		}
		return tuple5.getValue5().equals(this.getValue5());
	}

}