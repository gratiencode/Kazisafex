/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

import jakarta.persistence.EntityManager;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 *
 * @author endeleya
 */
public class WriteTask<T> {

    private final Function<EntityManager, T> action;
    private final CompletableFuture<T> future;

    public WriteTask(Function<EntityManager, T> action, CompletableFuture<T> future) {
        this.action = action;
        this.future = future;
    }

    public Function<EntityManager, T> getAction() {
        return action;
    }

    public CompletableFuture<T> getFuture() {
        return future;
    }
}

