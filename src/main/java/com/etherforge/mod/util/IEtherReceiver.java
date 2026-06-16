// util/IEtherReceiver.java
package com.etherforge.mod.util;

public interface IEtherReceiver {
    /**
     * Принять эфир.
     * @param amount   сколько хотим передать
     * @param simulate если true — только проверка, без реального переноса
     * @return сколько фактически принято
     */
    int receiveEther(int amount, boolean simulate);

    int getEtherStored();
    int getMaxEther();

    /** Может ли принять хотя бы 1 единицу */
    default boolean canReceiveEther() {
        return getEtherStored() < getMaxEther();
    }
}