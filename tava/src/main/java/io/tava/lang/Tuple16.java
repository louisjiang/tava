package io.tava.lang;

import io.tava.function.Function1;

public class Tuple16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> implements Tuple {

	private final T1 value1;

	private final T2 value2;

	private final T3 value3;

	private final T4 value4;

	private final T5 value5;

	private final T6 value6;

	private final T7 value7;

	private final T8 value8;

	private final T9 value9;

	private final T10 value10;

	private final T11 value11;

	private final T12 value12;

	private final T13 value13;

	private final T14 value14;

	private final T15 value15;

	private final T16 value16;

	public Tuple16(T1 value1, T2 value2, T3 value3, T4 value4, T5 value5, T6 value6, T7 value7, T8 value8, T9 value9, T10 value10, T11 value11, T12 value12, T13 value13, T14 value14, T15 value15, T16 value16) {
		this.value1 = value1;
		this.value2 = value2;
		this.value3 = value3;
		this.value4 = value4;
		this.value5 = value5;
		this.value6 = value6;
		this.value7 = value7;
		this.value8 = value8;
		this.value9 = value9;
		this.value10 = value10;
		this.value11 = value11;
		this.value12 = value12;
		this.value13 = value13;
		this.value14 = value14;
		this.value15 = value15;
		this.value16 = value16;
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

	public T8 getValue8() {
		return this.value8;
	}

	public T9 getValue9() {
		return this.value9;
	}

	public T10 getValue10() {
		return this.value10;
	}

	public T11 getValue11() {
		return this.value11;
	}

	public T12 getValue12() {
		return this.value12;
	}

	public T13 getValue13() {
		return this.value13;
	}

	public T14 getValue14() {
		return this.value14;
	}

	public T15 getValue15() {
		return this.value15;
	}

	public T16 getValue16() {
		return this.value16;
	}

	public <R> R map(Function1<Tuple16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>, R> map) {
		return map.apply(this);
	}

	public int hashCode() {
		return this.value1.hashCode() ^ this.value2.hashCode() ^ this.value3.hashCode() ^ this.value4.hashCode() ^ this.value5.hashCode() ^ this.value6.hashCode() ^ this.value7.hashCode() ^ this.value8.hashCode() ^ this.value9.hashCode() ^ this.value10.hashCode() ^ this.value11.hashCode() ^ this.value12.hashCode() ^ this.value13.hashCode() ^ this.value14.hashCode() ^ this.value15.hashCode() ^ this.value16.hashCode();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Tuple16)) {
			return false;
		}
		Tuple16 tuple16 = (Tuple16) obj;
		if (this == tuple16) {
			return true;
		}
		if (!tuple16.getValue1().equals(this.getValue1())) {
			return false;
		}
		if (!tuple16.getValue2().equals(this.getValue2())) {
			return false;
		}
		if (!tuple16.getValue3().equals(this.getValue3())) {
			return false;
		}
		if (!tuple16.getValue4().equals(this.getValue4())) {
			return false;
		}
		if (!tuple16.getValue5().equals(this.getValue5())) {
			return false;
		}
		if (!tuple16.getValue6().equals(this.getValue6())) {
			return false;
		}
		if (!tuple16.getValue7().equals(this.getValue7())) {
			return false;
		}
		if (!tuple16.getValue8().equals(this.getValue8())) {
			return false;
		}
		if (!tuple16.getValue9().equals(this.getValue9())) {
			return false;
		}
		if (!tuple16.getValue10().equals(this.getValue10())) {
			return false;
		}
		if (!tuple16.getValue11().equals(this.getValue11())) {
			return false;
		}
		if (!tuple16.getValue12().equals(this.getValue12())) {
			return false;
		}
		if (!tuple16.getValue13().equals(this.getValue13())) {
			return false;
		}
		if (!tuple16.getValue14().equals(this.getValue14())) {
			return false;
		}
		if (!tuple16.getValue15().equals(this.getValue15())) {
			return false;
		}
		return tuple16.getValue16().equals(this.getValue16());
	}

}