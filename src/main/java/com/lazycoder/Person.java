package com.lazycoder;

import com.lazycoder.db.annotation.Column;
import com.lazycoder.db.annotation.Entity;
import com.lazycoder.db.annotation.Id;

@Entity(tableName = "person")
public record Person(
    @Id
    @Column(name = "id")
    String id,

    @Column(name = "name")
    String name
) {}
