package org.example;

import java.util.List;

public class Main {
    public int sub(int a, int b) {
		return a - b;
	}

    public int add(int a, int b) {
		return a + b;
	}

    public int mul (int a, int b) {
        return b * a;
    }

    public int div (int a, int b) {
        return a / b;
    }

    public int max (List<Integer> list) {
        int res = 0;
        for (Integer i : list) {
            if (res < i) {
                res = i;
            }
        }
        return res;
    }
}
