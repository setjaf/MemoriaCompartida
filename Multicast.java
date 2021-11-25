/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.memoriacompartida;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author setjafet
 */
public class Multicast{
    
    InetAddress ip_grupo;
    MulticastSocket socket;
    int puerto = 50000;
    String ip = "228.1.1.1";
    public boolean cambio=false;
    public int localidad = -1;
    public long numero = -1;
    
    public Multicast() throws IOException {
        ip_grupo = InetAddress.getByName(ip);
        socket = new MulticastSocket(puerto);
        socket.setReuseAddress(true);
        socket.joinGroup(ip_grupo);
    }
    
    private byte[] recibe_mensaje(int longitud_mensaje) throws IOException{
        byte[] buffer =  new byte[longitud_mensaje];
        DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
        socket.receive(paquete);
        //System.out.println("Se recibió un mensaje");
        return paquete.getData();
    }
    
    private void envia_mensaje(byte[] buffer) throws IOException{
        DatagramSocket socket = new DatagramSocket();
        InetAddress ip_grupo = InetAddress.getByName(ip);
        DatagramPacket paquete = new DatagramPacket(buffer, buffer.length,ip_grupo,puerto);
        socket.send(paquete);
        socket.close();
    }
    
    public void enviar_cambios(int localidad, long numero) throws IOException{
        ByteBuffer b = ByteBuffer.allocate(12);
        b.putInt(localidad);
        b.putLong(numero);
        envia_mensaje(b.array()); 
    }

    public class Worker extends Thread{
        @Override
        public void run() {
            byte[] buffer;
            //System.out.println("Esperando Mensajes");
            try {
                for(;;){
                    buffer = recibe_mensaje(8+4);
                    ByteBuffer b = ByteBuffer.wrap(buffer); 

                    localidad = b.getInt();
                    numero = b.getLong();  
                    cambio = true;

                    //System.out.println("Nuevos Valores Localidad: "+ localidad+ " Numero: "+numero+" Cambio: "+cambio);
                }
            } catch (IOException ex) {
                Logger.getLogger(Multicast.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Super Error "+ex.getMessage() );
            }

        }
    }
    
    public class RevisarCambio extends Thread{
        
        @Override
        public void run() {
            for(;;){
                //System.out.println("Revisando cambio cambio: "+ cambio);
                if (cambio) {
                    System.err.println("Han cambiado los valores :), localidad: "+ localidad+ ", numero: "+ numero);
                    cambio=false;
                }
            }
        }

    } 

    
    public static void main(String[] args) throws IOException, InterruptedException {
        int incremento=0;
        Multicast m  = new Multicast();
        
        m.new Worker().start();
        //m.new RevisarCambio().start();
        
        for(;;){
            System.out.println("Porvocando cambio...\nm.Cambio: "+m.cambio);
            m.enviar_cambios(0, incremento);
            Thread.sleep(2500);
            incremento++;
        }
    }
    
}
     
