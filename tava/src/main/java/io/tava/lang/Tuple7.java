package io.tava.lang;

import io.tava.function.Function1;

public class Tuple7<T1, T2, T3, T4, T5, T6, T7> implements Tuple {

	private final T1 value1;

	private final T2 value2;

	private final T3 value3;

	private final T4 value4;

	private final T5 value5;

	private final T6 value6;

	private final T7 value7;

	public Tuple7(T1 value1, T2 value2, T3 value3, T4 value4, T5 value5, T6 value6, T7 value7) {
		this.value1 = value1;
		this.value2 = value2;
		this.value3 = value3;
		this.value4 = value4;
		this.value5 = value5;
		this.value6 = value6;
		this.value7 = value7;
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

	public T6 getValue6() {
		return this.value6;
	}

	public T7 getValue7() {
		return this.value7;
	}

	public <R> R map(Function1<Tuple7<T1, T2, T3, T4, T5, T6, T7>, R> map) {
		return map.apply(this);
	}

	public int hashCode() {
		return this.value1.hashCode() ^ this.value2.hashCode() ^ this.value3.hashCode() ^ this.value4.hashCode() ^ this.value5.hashCode() ^ this.value6.hashCode() ^ this.value7.hashCode();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Tuple7)) {
			return false;
		}
		Tuple7 tuple7 = (Tuple7) obj;
		if (this == tuple7) {
			return true;
		}
		if (!tuple7.getValue1().equals(this.getValue1())) {
			return false;
		}
		if (!tuple7.getValue2().equals(this.getValue2())) {
			return false;
		}
		if (!tuple7.getValue3().equals(this.getValue3())) {
			return false;
		}
		if (!tuple7.getValue4().equals(this.getValue4())) {
			return false;
		}
		if (!tuple7.getValue5().equals(this.getValue5())) {
			return false;
		}
		if (!tuple7.getValue6().equals(this.getValue6())) {
			return false;
		}
		return tuple7.getValue7().equals(this.getValue7());
	}

}