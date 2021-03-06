package io.tava.lang;

import io.tava.function.Function1;

public class Tuple22<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22> implements Tuple {

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

	private final T17 value17;

	private final T18 value18;

	private final T19 value19;

	private final T20 value20;

	private final T21 value21;

	private final T22 value22;

	public Tuple22(T1 value1, T2 value2, T3 value3, T4 value4, T5 value5, T6 value6, T7 value7, T8 value8, T9 value9, T10 value10, T11 value11, T12 value12, T13 value13, T14 value14, T15 value15, T16 value16, T17 value17, T18 value18, T19 value19, T20 value20, T21 value21, T22 value22) {
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
		this.value17 = value17;
		this.value18 = value18;
		this.value19 = value19;
		this.value20 = value20;
		this.value21 = value21;
		this.value22 = value22;
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

	public T17 getValue17() {
		return this.value17;
	}

	public T18 getValue18() {
		return this.value18;
	}

	public T19 getValue19() {
		return this.value19;
	}

	public T20 getValue20() {
		return this.value20;
	}

	public T21 getValue21() {
		return this.value21;
	}

	public T22 getValue22() {
		return this.value22;
	}

	public <R> R map(Function1<Tuple22<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22>, R> map) {
		return map.apply(this);
	}

	public int hashCode() {
		return this.value1.hashCode() ^ this.value2.hashCode() ^ this.value3.hashCode() ^ this.value4.hashCode() ^ this.value5.hashCode() ^ this.value6.hashCode() ^ this.value7.hashCode() ^ this.value8.hashCode() ^ this.value9.hashCode() ^ this.value10.hashCode() ^ this.value11.hashCode() ^ this.value12.hashCode() ^ this.value13.hashCode() ^ this.value14.hashCode() ^ this.value15.hashCode() ^ this.value16.hashCode() ^ this.value17.hashCode() ^ this.value18.hashCode() ^ this.value19.hashCode() ^ this.value20.hashCode() ^ this.value21.hashCode() ^ this.value22.hashCode();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Tuple22)) {
			return false;
		}
		Tuple22 tuple22 = (Tuple22) obj;
		if (this == tuple22) {
			return true;
		}
		if (!tuple22.getValue1().equals(this.getValue1())) {
			return false;
		}
		if (!tuple22.getValue2().equals(this.getValue2())) {
			return false;
		}
		if (!tuple22.getValue3().equals(this.getValue3())) {
			return false;
		}
		if (!tuple22.getValue4().equals(this.getValue4())) {
			return false;
		}
		if (!tuple22.getValue5().equals(this.getValue5())) {
			return false;
		}
		if (!tuple22.getValue6().equals(this.getValue6())) {
			return false;
		}
		if (!tuple22.getValue7().equals(this.getValue7())) {
			return false;
		}
		if (!tuple22.getValue8().equals(this.getValue8())) {
			return false;
		}
		if (!tuple22.getValue9().equals(this.getValue9())) {
			return false;
		}
		if (!tuple22.getValue10().equals(this.getValue10())) {
			return false;
		}
		if (!tuple22.getValue11().equals(this.getValue11())) {
			return false;
		}
		if (!tuple22.getValue12().equals(this.getValue12())) {
			return false;
		}
		if (!tuple22.getValue13().equals(this.getValue13())) {
			return false;
		}
		if (!tuple22.getValue14().equals(this.getValue14())) {
			return false;
		}
		if (!tuple22.getValue15().equals(this.getValue15())) {
			return false;
		}
		if (!tuple22.getValue16().equals(this.getValue16())) {
			return false;
		}
		if (!tuple22.getValue17().equals(this.getValue17())) {
			return false;
		}
		if (!tuple22.getValue18().equals(this.getValue18())) {
			return false;
		}
		if (!tuple22.getValue19().equals(this.getValue19())) {
			return false;
		}
		if (!tuple22.getValue20().equals(this.getValue20())) {
			return false;
		}
		if (!tuple22.getValue21().equals(this.getValue21())) {
			return false;
		}
		return tuple22.getValue22().equals(this.getValue22());
	}

}