package com.jj.wifi.singleton;

public class SingleTon {
    private static SingleTon singleTonHungry = new SingleTon();
    private static SingleTon singleTonLazy;
    private SingleTon(){}
    public static SingleTon getSingleTonHungry(){
        return singleTonHungry;
    }
    public static SingleTon getSingleTonLazy(){
        if(singleTonLazy == null){
            synchronized (SingleTon.class){
                if(singleTonLazy == null){
                    singleTonLazy = new SingleTon();
                }
            }
        }
        return singleTonLazy;
    }
}
