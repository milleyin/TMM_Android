// IMyAidlInterface.aidl
package com.qljl.tmmdp.reciver;

// Declare any non-default types here with import statements

interface IMyAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    /**
    * 获取本app的版本
    */
    int haveUpdate();

    /**
    * 更新主app
    */
    void updateApk(String apkName);
}
