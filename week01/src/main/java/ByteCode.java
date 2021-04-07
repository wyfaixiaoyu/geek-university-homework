public class ByteCode {
	public static void caculate() {
		int a = ChildClass.a;
		int b = 5;
		int c = a + b;
		int d = c / b;
		int e = c * d;

		System.out.println(e);
	}
}
