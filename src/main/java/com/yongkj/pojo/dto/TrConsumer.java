package com.yongkj.pojo.dto;

@FunctionalInterface
public interface TrConsumer<R, T, U> {

    void accept(R r, T t, U u);

}
