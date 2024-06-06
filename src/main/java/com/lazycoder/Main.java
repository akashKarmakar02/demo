package com.lazycoder;

import java.io.IOException;
import java.sql.SQLException;

import com.amberj.net.httpserver.Server;
import com.lazycoder.db.Database;


public class Main {
    // static @interface val{}
    public static void main(String[] args) throws IOException, InterruptedException {
        Database db = new Database();
        var server = new Server(8000);

        server.handle("/", new IndexHandler());
        
        server.run();
    }
}
