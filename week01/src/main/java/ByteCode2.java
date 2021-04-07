public class ByteCode2 {
	public static void main(String[] args) {
		int num = 10;
		System.out.println(test1(num));
		System.out.println(test2(num));
	}

	public static int test1(int a) {
		try {
			a += 20;
			return a;
		} finally {
			a += 30;
			return a;
		}
	}

	public static int test2(int a) {
		try {
			a += 20;
			return a;
		} finally {
			a += 30;
			System.out.println(a);
		}
	}
}
