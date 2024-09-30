package com.azure.recipes.core.v2;

import java.io.IOException;

public class UserClass {

    private int myMethod() throws IOException {
return 1;
    }
    private void myMethod2(){
        int a = 0;
        try {
            a = myMethod();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int b = a;
    }
    private void myMethod3(){
        try {
            myMethod();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
