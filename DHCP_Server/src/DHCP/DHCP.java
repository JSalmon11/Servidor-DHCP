package DHCP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class DHCP {

    private static final String IPSERVER   =   "10.0.2.2";
    private static final String IP         =   "10.0.2.120";
    private static final String ROUTER     =   "10.0.2.1";
    private static final String MASCARA    =   "255.255.255.0";
    private static final String DNS        =   "8.8.8.8";
    private static final int TIEMPOCESION = 60;
    private static final int TIEMPORENOVACION = TIEMPOCESION / 2;

    public static void main(String[] args) {
        DatagramSocket s = getSocket();
        boolean clienteConfigurado = false;
        while (!clienteConfigurado) {
            //El servidor recibe el mensaje por el puerto 67
            byte[] msgCliente = recibirPeticion(s);
            byte[] transactionID = Arrays.copyOfRange(msgCliente, 4, 8);
            byte[] MAC = Arrays.copyOfRange(msgCliente, 28, 44);
            //Calcula el comienzo de las opicones
            int iniOptions = findEndMagicCookie(msgCliente);
            //Metodo para sacar la informacion de cualquier opcion
            byte[] opcionTipoMsg = sacarOpcion(msgCliente, iniOptions, 53);
            
            switch (opcionTipoMsg[0]) {
                case 1:
                //1, es un discover, se responde con un offer
                    enviarRespuesta(transactionID, MAC, s, (byte)2);
                    break;
                case 3:
                //3, es un request, se responde con un ack
                    enviarRespuesta(transactionID, MAC, s, (byte)5);
                    clienteConfigurado = true;
                    break;
            }
        }
        //El cliente ha recibido un ack, ya
        //ha sido configurado y muere el servidor
        s.close();
    }// main()
    
    private static int findEndMagicCookie(byte[] msgCliente) {
        //Devuelve la posicion del array en la que empizan
        //las opciones (el fin de la magic cookie)
        int endMC = -1;
        for (int i = 0; i < msgCliente.length && endMC == -1; ++i) {
            if (msgCliente[i] == 99 && msgCliente[i + 1] == -126
            && msgCliente[i + 2] == 83 && msgCliente[i + 3] == 99) {
                endMC = i + 4;
            }
        }
        return endMC;
    }// findEndMagicCookie()

    private static byte[] sacarOpcion(byte[] msgCliente, int inicio, int cod) {
        byte[] opcion = null;
        int longitud;
        for (int i = inicio; i < msgCliente.length && opcion == null; ++i) {
            //lee la opcion, si no coincide con el codigo solicitado
            //lee esa opcion para pasar a la siguiente
            if (msgCliente[i] == cod) {
                longitud = (int)msgCliente[i + 1];
                opcion = Arrays.copyOfRange(msgCliente, i + 2, i+2+longitud);
            }else{
                longitud = (int)msgCliente[i + 1];
                i+=1+longitud;
            }
        }
        return opcion;
    }// sacarOpcion()

    private static void enviarRespuesta(byte[] transactionID, byte[] MAC, DatagramSocket s, byte type) {
        //Segun si es un offer o un ack recibe por parametro (type),
        //el byte necesario que compone la respuesta adecuada
        ByteBuffer Offer = ByteBuffer.allocate(576);
        
        //CABECERA
        //Message type
        Offer.put((byte)2);
        //Hardware type
        Offer.put((byte)1);
        //Hardware address length
        Offer.put((byte)6);
        //Hops
        Offer.put((byte)0);
        Offer.put(transactionID);
        //secs
        Offer.putShort((short)0);
        //flags
        Offer.putShort((short)1);
        //Ip cliente
        Offer.put(new byte[4]);
        //Ip ofrecida
        Offer.put(toByteArray(IP));
        //Ip siguiente DHCP server
        Offer.put(new byte[4]);
        //Ip relay agent
        Offer.put(new byte[4]);
        Offer.put(MAC);
        //Server hostname
        Offer.put(new byte[64]);
        //Boot filename
        Offer.put(new byte[128]);
        byte[] magicCookie = {99,(byte)130,83,99};
        
        //OPTIONS
        Offer.put(magicCookie);
            //Opcion                //Length                //Contenido opcion
        Offer.put((byte)53);    Offer.put((byte)1);     Offer.put(type);
        Offer.put((byte)1);     Offer.put((byte)4);     Offer.put(toByteArray(MASCARA));
        Offer.put((byte)6);     Offer.put((byte)4);     Offer.put(toByteArray(DNS));
        Offer.put((byte)3);     Offer.put((byte)4);     Offer.put(toByteArray(ROUTER));
        Offer.put((byte)51);    Offer.put((byte)4);     Offer.put((byte)TIEMPOCESION);
        Offer.put((byte)58);    Offer.put((byte)4);     Offer.put((byte)TIEMPORENOVACION);
        Offer.put((byte)54);    Offer.put((byte)4);     Offer.put(toByteArray(IPSERVER));
        Offer.put((byte)255);
        
        send(Offer, s);
    }// enviarRespuesta()

    private static byte[] toByteArray(String StringToByte) {
        //Transforma una direccion en un array de bytes
        byte[] convertido = null;
        try {
            convertido = InetAddress.getByName(StringToByte).getAddress();
        } catch (UnknownHostException e) {
            System.out.println(e);
        }
        return convertido;
    }// toByteArray

    private static void send(ByteBuffer Offer, DatagramSocket s) {
        //Envia la respuesta al cliente mediante broadcast
        try {
            InetAddress BROADCAST = InetAddress.getByName("255.255.255.255");
            DatagramPacket paquete = new DatagramPacket(Offer.array(), Offer.array().length, BROADCAST, 68);
            s.send(paquete);
        }
        catch (IOException ex) {
            System.out.println(ex);
        }
    }// send()

    private static byte[] recibirPeticion(DatagramSocket s) {
        byte[] buffer = new byte[576];
        DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
        try {
            s.receive(paquete);
            buffer = paquete.getData();
        } catch (IOException e) {
            System.out.println(e);
        }
        return buffer;
    }// recibirMensaje()

    private static DatagramSocket getSocket() {
        DatagramSocket s = null;
        try {
            s = new DatagramSocket(67);
        } catch (SocketException e) {
            System.out.println(e);
        }
        return s;
    }// getSocket()

}// DHCP