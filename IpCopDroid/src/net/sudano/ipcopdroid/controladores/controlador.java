package net.sudano.ipcopdroid.controladores;

import android.app.Activity;
import android.widget.Toast;

import net.sudano.ipcopdroid.util.conexionPorSSH;

import java.io.IOException;

/**
 * Created by usuario on 15/07/13.
 */
public class controlador {
    private Activity ActividadPrincipal;

    /** Este metodo es el constructor y sirve para pasar el Contexto de la Aplicación a la clase
     *  Así de esa manera podemos usar todos los metodos que pongamos dentro de esta clase
     *  sobra decir que hay que tener cuidado que metodos ponemos aquí.
     */
    public controlador(Activity actividad){
        this.ActividadPrincipal = actividad;
    }

    public void Mensaje(String mensaje){
        // Creating Toast Dialog
        Toast.makeText(ActividadPrincipal, mensaje, Toast.LENGTH_LONG).show();
    }

    public void conectar() throws IOException {
        conexionPorSSH conexion = new conexionPorSSH(ActividadPrincipal,"192.168.1.5",22,"usuario","c0ntras3#a",3000);
        //conexion.execute();
        if (conexion.sessionConectado()){
            conexion.dseconectar();
        }
    }
}
