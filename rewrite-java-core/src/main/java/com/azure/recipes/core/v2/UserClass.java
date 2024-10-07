package com.azure.recipes.core.v2;

import java.io.IOException;

        public class UserClass {
            public UserClass(){}
            String s = "Hello";

            public String myMethod() {
               return s;
            }
        }
        class UserClass2 {
            public void myMethod2() {
                UserClass c = new UserClass();
                String s2 = c.myMethod();
            }
        }
