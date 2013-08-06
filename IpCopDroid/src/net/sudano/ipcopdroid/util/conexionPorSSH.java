package net.sudano.ipcopdroid.util;

import android.app.Activity;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by usuario on 14/07/13.
 */
public class conexionPorSSH {
    private Activity _activity; //Esta será la actividad desde la que se ha llamado la conexión
    private String _servidor; // Direccion IP del servidor al que queremos conectarnos
    private int _puerto; // Este es el puerto por defecto que usara la conexión
    private String _usuario; // Este es el usuario para la conexión
    private String _pass; // Esta es la contraseña de la conexión

    JSch objetoJSCH = new JSch(); // Este objeto es para la conexión SSH
    Session _session = null; // Este objeto es para la session de SSH en un momento dado.
    Channel _channel= null; // Este es el objeto que usamos para ejecutar comandos

    private static final int DEFAULT_CONNECT_TIMEOUT = 0;
    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;

    public conexionPorSSH(Activity activity, String servidor, Integer puerto, String usuario, String pass, Integer timeout){
        this._activity = activity;
        this._servidor = servidor;
        this._puerto = puerto;
        this._usuario = usuario;
        this._pass = pass;
        this.connectTimeout = timeout;
    }

    public void conectar() throws JSchException{
        /**if (_usuario.isEmpty()){throw new JSchException("Falta Usuario por definir");}
         *if (_servidor.isEmpty()){throw new JSchException("Falta la ip para la conexión");}
         *if (_puerto<=0){throw new JSchException("El puerto esta vacio");}else if (compruebaPuerto()){throw new JSchException("El puerto no es válido");}
        **/
        _session = objetoJSCH.getSession(_usuario,_servidor, _puerto);
        _session.setPassword(_pass);

        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no"); // Evitar pedir confirmación de clave
        prop.put("PreferredAuthentications", "password,keyboard-interactive"); // Metodos preferidos de Autentificación
        _session.setConfig(prop);

        _session.connect(connectTimeout);
    }

    public String ejecutar(String comando) throws JSchException, IOException{
        String resultado = null;
             _channel=_session.openChannel("exec");
            ((ChannelExec)_channel).setCommand(comando);
            ((ChannelExec)_channel).setErrStream(System.err);
            InputStream in=_channel.getInputStream();
            _channel.connect();

            byte[] tmp=new byte[1024];
            while(true){
                while(in.available()>0){
                    int i=in.read(tmp, 0, 1024);
                    if(i<0)break;
                    resultado = resultado + new String(tmp, 0, i);
                }
                if(_channel.isClosed()){
                    resultado = resultado + "exit-status: "+_channel.getExitStatus();
                    break;
                }
                try{Thread.sleep(1000);}catch(Exception ee){}
            }
        return resultado;
    }

    /**
     * Esta función es para saber si esta conectada la session
     * @return devuelve true si esta conectada la session o false en caso contrario
     */
    public boolean sessionConectado(){
        boolean estado = false;
        if (_session.isConnected()){
            estado = true;
        }
        return estado;
    }

    /**
     * Esta función es para saber si esta conectada el canal
     * @return devuelve true si esta conectada el canal o false en caso contrario
     */
    public boolean canalConectado(){
        boolean estado = false;
        if (_session.isConnected()){
            estado = true;
        }
        return estado;
    }

    /**
     * Esta función sirve para desconectar la session y el canal de ejecución
     */
    public void dseconectar() {
        if (sessionConectado()){
            _session.disconnect();
        }
        if (canalConectado()){
            _channel.disconnect();
        }
        objetoJSCH = null;
    }

    /**
     * @return Devuelve true si el puerto esta correcto y false si no lo esta
     */
    private boolean compruebaPuerto() {
        boolean valor = false;
        int c = 0;
        while (variablesApp.DEFAULT_PORTS_TO_CONNECT.length < c){
            if(_puerto == variablesApp.DEFAULT_PORTS_TO_CONNECT[c]){
                valor = true;
                break;
            }
            c=c+1;
        }
        return valor;
    }
}
