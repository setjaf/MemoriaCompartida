/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.memoriacompartida;

import java.io.IOException;

/**
 *
 * @author setjafet
 */
public class Prog1 {
    
    static long[] M;
    static boolean[] B;
    static int nodo;
    static String hosts[];
    static int ports[];
    static Multicast m;
    static Token t;
    
    public static class Revisar extends Thread{

        @Override
        public void run() {
            super.run(); //To change body of generated methods, choose Tools | Templates.
            for(;;){
                //System.out.println("Revisando cambio cambio: "+ cambio);
                if (m.cambio) {
                    System.err.println("Han cambiado los valores :), localidad: "+ m.localidad+ ", numero: "+ m.numero);
                    M[m.localidad]=m.numero;
                    m.cambio=false;
                }
            }
        }        
               
    }
    
    public static void lock() throws InterruptedException{
        t.lock();
        for (int i = 0; i < hosts.length; i++) {
            B[i]=false;
        }
    }
    
    public static long Read(int localidad){
        
        return M[localidad];
    }
    
    public static void Write(int localidad, long valor){
        M[localidad]=valor;
        B[localidad]=true;
    }
    
    public static void unlock() throws IOException, InterruptedException{
        
        for (int i = 0; i < hosts.length; i++) {
            if(B[i]){
                m.enviar_cambios(i, M[i]);
            }
        }
        
        t.unlock();
    }
    
    public static void main(String[] args) throws IOException, InterruptedException {
        if(args.length<=1){
            System.err.println("Hacen falta argumentos");
            return;
        }
        
        hosts = new String[args.length-1];
        ports = new int[args.length-1];
        
        nodo = Integer.valueOf(args[0]);
        
        for(int i = 1; i < args.length; i++){
            String[] div = args[i].split(":");
            hosts[i-1] = div[0];
            ports[i-1] = Integer.valueOf(div[1]);
        }
        
        int siguienteNodo=nodo+1>=hosts.length?0:nodo+1;        
        
        t = new Token(nodo, ports[nodo], ports[siguienteNodo]);
        
        t.iniciarToken(hosts[nodo]);
        
        m = new Multicast();
        
        new Revisar().start();
        
        m.new Worker().start();
        
        
        M = new long[hosts.length];
        B = new boolean[hosts.length];
        
        long r;
        for(int i = 0; i<3; i++){
            lock();
            r = Read(0);
            r++;
            Write(0, r);
            Thread.sleep(10000);
            unlock();
        }
        
        if(nodo==0){
            lock();
            r = Read(0);
            unlock();
            System.out.println("El valor final es: " + r);
        }
        
    }
    
}
