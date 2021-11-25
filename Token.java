/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.memoriacompartida;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author setjafet
 */
public class Token {
    
    int[] puertos;
    int nodo;
    int token = -1;
    Object obj = new Object();
    boolean nodoAnterior = false;
    Socket socket;
    boolean solicitando_recurso=false;

    public Token(int nodo, int puertoLocal, int puertoRemoto) throws InterruptedException, IOException {
        this.nodo = nodo;
        this.puertos = new int[2];
        this.puertos[0]=puertoLocal;
        this.puertos[1]=puertoRemoto;
        
    }
    
    public class ServidorToken extends Thread{

        @Override
        public void run() {
            try {
                ServerSocket servidor = new ServerSocket(puertos[0]);
                Socket socket = servidor.accept();
                WorkerToken w = new WorkerToken(socket);
                w.start();
            } catch (IOException ex) {
                Logger.getLogger(Token.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        
    }
    
    public class WorkerToken extends Thread {
        Object obj = new Object();
        Socket soc;

        public WorkerToken(Socket s) {
            this.soc = s;
        }

        @Override
        public void run() {
            System.out.println("Inicio el Thread Worker");
            try {
                DataInputStream entrada = new DataInputStream(soc.getInputStream());
                DataOutputStream salida = new DataOutputStream(soc.getOutputStream());
                for(;;){
                    int tipo = entrada.readInt();
                    switch(tipo){ // 0:Hello 1:RecibirToken
                        case 0:
                            //System.out.println("Se recibió saludo");
                            salida.writeBoolean(true);
                            nodoAnterior = true;
                            break;
                        case 1:
                            //System.out.println("Se recibió token");
                            synchronized(obj){
                                token = entrada.readInt();
                                System.out.println("Se recibió el token : "+token);
                            } 
                            System.out.println(solicitando_recurso);
                            if(!solicitando_recurso){
                                try {
                                    envia_mensaje(2,socket);
                                    synchronized(obj){
                                        token = -1;
                                    }
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(Token.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            break;                        
                        default:
                            System.out.println("Default");
                    } 
                }               
            } catch (IOException ex) {
                Logger.getLogger(Token.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } 
        
        
    }
    
    public boolean envia_mensaje(int tipo, Socket soc) throws IOException, InterruptedException{
        
        DataOutputStream salida = new DataOutputStream(soc.getOutputStream());
        DataInputStream entrada = new DataInputStream(soc.getInputStream());
        
        switch(tipo){
            case 0: // envia Saludo
                salida.writeInt(tipo);
                return entrada.readBoolean();
            case 1: // Envia token cuando se usa
                salida.writeInt(tipo); 
                salida.writeInt(nodo);
                return true;
            case 2: // Envia token cuando no se usa
                //System.out.println("Enviando no se usa");
                salida.writeInt(1); 
                salida.writeInt(token);
                return true;
            default:
                return false;
        }
        
    }
    
    public void iniciarToken(String host, int puerto) throws InterruptedException, IOException{
        socket = null;
        for(;;){
            try {  
                socket = new Socket(host, puerto);
                break;
            } catch (IOException ex) {
                
                System.err.print("\rError, intentando de nuevo ...");
                Thread.sleep(1000);
                
                //            Logger.getLogger(Prog2.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        envia_mensaje(0, socket);
        
        for(;;){
            System.out.println("Esperando anterior");
            if(nodoAnterior){
                if(nodo==0){
                    System.out.println("Iniciamos");
                    token = 0;
                }
                break;
            }
        }
    }
    
    public void iniciarToken(String host) throws InterruptedException, IOException{

        new ServidorToken().start();
        
        socket = null;
        for(;;){
            try {  
                socket = new Socket(host, puertos[1]);
                break;
            } catch (IOException ex) {
                
                System.err.print("\rError, intentando de nuevo ...");
                Thread.sleep(1000);
                
                    //            Logger.getLogger(Prog2.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        envia_mensaje(0, socket);
        
        for(;;){
            //System.out.println("Esperando anterior");
            if(nodoAnterior){
                if(nodo==0){
                    System.out.println("Iniciamos");
                    token = 0;
                }
                break;
            }
        }
    }
    
    public int lock() throws InterruptedException{
        solicitando_recurso = true;
        System.out.println("Solicitando recurso");
        for(;;){
            if(nodo==2)
                
            if(token != -1){
                System.out.println("---------------------------------------------------------------------------");
                System.out.println("Recurso adquirido, en uso");
                //Thread.sleep(500);
                return token;
            }
            //System.out.print(".");
        }
    }
    
    public void unlock() throws IOException, InterruptedException{
        solicitando_recurso = false;
        envia_mensaje(1, socket);
        synchronized(obj){
            token = -1;
        }
        
        System.out.println("Recurso Liberado");
        System.out.println("---------------------------------------------------------------------------");
    }
    
    
//    public static void main(String[] args) throws InterruptedException, IOException {
//        if(args.length < 3){
//            System.err.println("Error, faltan argumentos");
//            return;
//        }
//        
//        nodo = Integer.valueOf(args[0]);
//        puertos = new int[2];
//        puertos[0] = Integer.valueOf(args[1]);
//        puertos[1] = Integer.valueOf(args[2]);
//        
//        new ServidorToken().start();
//        
//        iniciarToken("127.0.0.1", puertos[1]);
//        if(nodo!=2){
//            for(;;){
//                lock();
//                System.out.println("Prueba");
//                Thread.sleep(2000);
//                unlock();
//            }
//        }
//    }
}
