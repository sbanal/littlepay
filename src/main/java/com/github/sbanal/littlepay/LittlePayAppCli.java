package com.github.sbanal.littlepay;


public class LittlePayAppCli {

    public static void main(String... args) {
        if (args.length < 1) {
            System.out.println("java -jar littlepay-cli.jar <input csv>");
            return;
        }
        LittlePayAppCli littlePayAppCli = new LittlePayAppCli();
        littlePayAppCli.hello();
    }

    public void hello() {
        System.out.println("Hello");
    }

}
