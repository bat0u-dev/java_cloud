package com.geekbrains.roganov.server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class DBConnector {
    private static Connection connection;

    public static Connection getConnection() {
        return connection;
    }

    public static boolean connectToDB(){//решить будет ли работа с базой в этом же классе или вынесена в отдельный?!
        Scanner userInput = new Scanner(System.in);
        String driver = "com.mysql.cj.jdbc.Driver";
        String dbURL = "jdbc:mysql://localhost:3306";
        String dbName = "personnel";
        String username = "root";
        String password = "MASTERKEY";
        try {
            Class.forName(driver).getDeclaredConstructor().newInstance();
//            System.out.println("Введите наименование базы данных");
//            dbName = userInput.nextLine();
//            System.out.println("Введите логин");
//            username = userInput.nextLine();
//            System.out.println("Введите пароль");
//            password = userInput.nextLine();//временное решение, нужно убрать работу с консолью и переделать под поля ввода
            //консоли управления сервером
            connection = DriverManager.getConnection(dbURL + "/" + dbName +
                    "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", username,password);
            if(connection!=null) {
                System.out.println("Connection successful!");
            } else {
                System.out.println("Connection failed...");
            }
            return true;
        } catch (Exception e) {
            System.out.println("Connection failed...");
            e.printStackTrace();
            return false;
        } finally {
//            try {
//                connection.close();
//            } catch (SQLException throwables) {
//                throwables.printStackTrace();
//            }//закрыть правильно!
        }
    }
    public static void main(String[] args) {
        //java -classpath c:\Java\mysql-connector-java-8.0.11.jar;c:\Java Program
        connectToDB();
        try {
            Statement stmt = connection.createStatement();
            String query = "SELECT * FROM personnel.employees";
            ResultSet rs = stmt.executeQuery(query);
            ArrayList<String> list = new ArrayList<>();
            while(rs.next()){
                list.add(rs.getString("name"));
            }
            System.out.println(list);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}

