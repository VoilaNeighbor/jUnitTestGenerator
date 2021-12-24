package cut;

import java.util.ArrayList;
import java.util.List;

public class LogicStructure {

	public int sequence(int a, int b) {
		return a + b;
	}

	public boolean soloIf(int op) {
		if (op > 0) {
			return true;
		}
		return false;
	}

	public boolean ifElse(int op) {
		if (op > 18) {
			return true;
		} else {
			return false;
		}
	}

	public String multipleIf(int op) {
		if (op % 15 == 0) {
			return "FizzBuzz";
		} else if (op % 5 == 0) {
			return "Buzz";
		} else if (op % 3 == 0) {
			return "Fizz";
		} else {
			return Integer.toString(op);
		}
	}

	public boolean myIf(int op) {
		if (op > 0) {
			return false;
		}
		if (op == 0) {
			return true;
		}
		return false;
	}

	public int myIf2(int op) {
		if (op == 0) {
			return 1;
		}
		if (op < 0) {
			op *= 3;
		} else {
			op += 2;
		}
		return op;
	}

	public int mywhile2(int op) {
		Foo a = new Foo();
		op = a.bar(op);
		return op;
	}

	public int mywhile(int op) {
		int n = 1;
		while (op > 0) {
			n *= 2;
			op -= 1;
		}
		return n;
	}
	public int for2(int op) {
		int n = 1;
		for(int i = 0;i < 3;i++){
			n = n * op;
		}
		return n;
	}

	public int myfor(int a,int b) {
		int result = 0;
		for (int i = 0; i <= b; ++i) {
			result += i;
		}
		if (result < 5) {
			for (int i = 0; i <= a; ++i) {
				result += 1;
			}
		}
		return result;
	}
	public List<Integer> Arraryfor2(int op) {
		List<Integer>  a = new ArrayList<>();
		for (int i = 0; i < op; i++) {
			a.add(i);
		}
		return a;
	}


	public class Foo {
		int bar(int n) {
			return n + 13;
		}
	}
	public int arrayTest(int[] a){
		if(a.length != 0){
			return a[0];
		}
		return 1;
	}
}
