package com.azure.recipes.core.v2;

import java.io.IOException;

public class UserClass {
    public UserClass() {}
    String s = "Hello";
    public String myMethod() {
       return s;
    }
}
class UserClass2 {
    UserClass c = new UserClass();
}
class UserClass3 {
    UserClass2 c2 = new UserClass2();
    public void myMethod3() {
        String s = c2.c.myMethod();
    }
}
