package ru.itmo.advancedjava.grpcdemo.domain;

import ru.itmo.advancedjava.grpcdemo.proto.Dude;

public record MyDude(
    String name,
    int age
) {

    public Dude.Builder toProto() {
        return Dude.newBuilder()
            .setName(name)
            .setAge(age);
    }
}
