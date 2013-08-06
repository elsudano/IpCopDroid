package net.sudano.ipcopdroid.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by usuario on 21/07/13.
 */

public class networkUtil {
    private Activity ActividadPrincipal;
    private Pattern patternIP;
    private Pattern patternURL;
    private Matcher matcher;
    // Expresión Regular para la validación de una IP
    private static final String IPADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    // Expresión Regular para la validación de una URL
    private static final String URL_PATTERN =
            "^(https?|ftp|file)://\\" +
            "[-a-zA-Z0-9+&@#/%?=~_|\\" +
            "!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

    public networkUtil(Activity actividad){
        this.ActividadPrincipal = actividad;
        patternIP = Pattern.compile(IPADDRESS_PATTERN); // esta linea sirve para la validacion de IP
        patternURL = Pattern.compile(URL_PATTERN); // esta linea sirve para la validacion de una URL
    }

    /**
     * Este metodo es para saber si el dispositivo esta
     * conectado a una red ya sea via Wifi o via 3G
     * @return verdadero si se conecta y falso si no lo consigue
     */
    public boolean isConnectingToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager) ActividadPrincipal.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }

        }
        return false;
    }

    /**
     * Valida una IP con expresiones regulares
     * @param ip la ip para la validación
     * @return verdadero si la IP es valida, falso si la IP no vale
     */
    private boolean validaIP(final String ip){
        matcher = patternIP.matcher(ip);
        return matcher.matches();
    }

    /**
     * Valida una URL con expresiones regulares
     * @param url la ip para la validación
     * @return verdadero si la IP es valida, falso si la IP no vale
     */
    private boolean validaURL(final String url){
        matcher = patternURL.matcher(url);
        return matcher.matches();
    }

    /**
     * Este metodo sirve para realizar un ping a un host cualquiera
     * @param hostname este es el host al que le haremos ping
     * @return Cadena de texto con el resultado del ping
     * @throws IOException
     */
    public String pingText(final String hostname) throws IOException{
        String pingResult = "";
        if (validaIP(hostname) || validaURL(hostname)) {
            String pingCmd = "ping -c 3 " + hostname;
            Runtime r = Runtime.getRuntime();
            Process p = r.exec(pingCmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                pingResult  = pingResult + inputLine + "\n";
            }
            in.close();
        }else{
            pingResult="La dirección del Hostname nos es valida";
        }
        return pingResult;
    }

    public Boolean pingBol(final String hostname) throws IOException{
        Boolean pingResult = false;
        if (validaIP(hostname) || validaURL(hostname)) {
            String pingCmd = "ping -c 3 " + hostname;
            Runtime r = Runtime.getRuntime();
            Process p = r.exec(pingCmd);
            if (p.exitValue() == 0){
                pingResult = true;
            }
        }
        return pingResult;
    }
}
