package org.example.scratch;

interface i1{
    int a=1;
}

interface i2{
    int a=2;
}

class exampleInterface implements i1,i2{
    public static void main(String...a){

        //As interface members are static we can write like this
        //If its not static then we'll write sysout(a) ... which gives ambiguity error.
        //That's why it is static.

//        try{
//            i2.a = 10;
//        } catch (Exception e){
//            System.out.println("THIS IS XYZ");
//        }
//        System.out.println(i2.a);
    }
}