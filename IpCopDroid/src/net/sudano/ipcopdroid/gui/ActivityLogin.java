package net.sudano.ipcopdroid.gui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.jcraft.jsch.JSchException;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;

import net.sudano.ipcopdroid.R;
import net.sudano.ipcopdroid.util.conexionPorSSH;
import net.sudano.ipcopdroid.util.networkUtil;
import net.sudano.ipcopdroid.util.variablesApp;

import java.io.IOException;


/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class ActivityLogin extends SlidingActivity {
    // Esta linea son para el menu lateral
    private SlidingMenu menu;

    // Esta linea es por si pasamos el correo electronico por medio de Intent ¿Creo?
    public static final String EXTRA_EMAIL = "android.intent.extra.EMAIL";
    public final Activity _activity = this;

    // Variables para los campos del formulario de Entrada
    private String mUser;
    private String mPassword;
    private String mEmail;
    private String mIPServer;
    private int mIPServerPort;

    // Referencias a la Interfaz grafica.
    private EditText mUserView;
    private EditText mPasswordView;
    private EditText mEmailView;
    private EditText mIPServerView;
    private EditText mIPServerPortView;
    private TextView mResultView;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);

        // seteamos la vista principal
        setContentView(R.layout.activity_login);

        // seteamos la vista del menu lateral
        setBehindContentView(R.layout.main_menu);

        // y con esto pedimos el menu deslizante para poder manejarlo
        menu = getSlidingMenu();

        // y con las siguientes lineas configuramos el menu lateral
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        menu.setShadowWidthRes(R.dimen.ShadowWidth);
        menu.setShadowDrawable(R.drawable.shadow);
        menu.setBehindOffsetRes(R.dimen.SlidingOffSet);
        menu.setFadeDegree(0.35f);

        // Esto sirve de manera cutre para quitar la excepcion de la red prohibida
        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);

        // Preparando el Formulario
        // Seteando el Usuario por defecto
        mUser = "usuario";
        mUserView = (EditText) findViewById(R.id.user);
        mUserView.setText(mUser);

        // Seteando la Contraseña
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    IntentarAutentificacion();
                    return true;
                }
                return false;
            }
        });

        // Seteando el Correo por defecto
        mEmail = getIntent().getStringExtra(EXTRA_EMAIL); // Esta linea es para cuando se pase el EMAIL por intent a la aplicación
        mEmail = "carlos@gmail.com";
        mEmailView = (EditText) findViewById(R.id.email);
        mEmailView.setText(mEmail);

        // Seteando el Servidor por Defecro
        mIPServer = "192.168.1.5";
        mIPServerView = (EditText) findViewById(R.id.ip_server);
        mIPServerView.setText(mIPServer);

        // Seteando el puerto del Servidor por Defecro
        mIPServerPort = 22;
        mIPServerPortView = (EditText) findViewById(R.id.port);
        mIPServerPortView.setText(Integer.toString(mIPServerPort));

        mResultView = (TextView) findViewById(R.id.result);

        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentarAutentificacion();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.menu_actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            menu.toggle();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (menu.isMenuShowing()) {
            menu.showContent();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void IntentarAutentificacion() {
        // Ponemos los Errores a 0.
        mUserView.setError(null);
        mPasswordView.setError(null);
        mEmailView.setError(null);
        mIPServerView.setError(null);
        mIPServerPortView.setError(null);

        // Guardamos los valores del formulario en unas variables.
        mUser = mUserView.getText().toString();
        mPassword = mPasswordView.getText().toString();
        mEmail = mEmailView.getText().toString();
        mIPServer = mIPServerView.getText().toString();
        mIPServerPort = Integer.parseInt(mIPServerPortView.getText().toString());

        boolean cancel = false;
        View focusView = null;

        // miramos que el campo de Usuario no este vacio
        // la validacion la haremos mas tarde
        if (TextUtils.isEmpty(mUser)) {
            mUserView.setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel = true;
        } else if (mUser.length() < 4) {
            mUserView.setError(getString(R.string.error_invalid_user));
            focusView = mUserView;
            cancel = true;
        }

        // Miramos que la contraseña sea valida
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (mPassword.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // miramos que el Email sea valido
        if (TextUtils.isEmpty(mEmail)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!mEmail.contains("@")) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        // miramos que el campo de IP servidor no este vacio
        // la validacion la haremos mas tarde
        if (TextUtils.isEmpty(mIPServer)) {
            mIPServerView.setError(getString(R.string.error_field_required));
            focusView = mIPServerView;
            cancel = true;
        }

        // miramos que el campo de Puerto de servidor no este vacio
        // la validacion la haremos mas tarde
        if (TextUtils.isEmpty(Integer.toString(mIPServerPort))) {
            mIPServerPortView.setError(getString(R.string.error_field_required));
            focusView = mIPServerPortView;
            cancel = true;
        }
        // para validar el puerto del servidor contamos con una variable en la aplicación
        // que seran los puertos permitidos en la aplicación y que es:
        // DEFAULT_PORTS_TO_CONNECT
        int c = 0;
        while (variablesApp.DEFAULT_PORTS_TO_CONNECT.length < c && cancel == false){
            if (mIPServerPort == variablesApp.DEFAULT_PORTS_TO_CONNECT[c]){
                // Puerto encontrado en la lista
            } else {
                mIPServerPortView.setError(getString(R.string.error_invalid_port));
                focusView = mIPServerPortView;
                cancel = true;
            }
            c=c+1;
        }

        if (cancel) {
            // Si encontramos un error en el formulario mandamos
            // el foco al campo con el error
            focusView.requestFocus();
        } else {
            /** Si el formulario esta correcto creamos los objetos que nos
             * hacen falta y ejecutamos las opciones en segundo plano
             **/

            networkUtil utilidades_red = new networkUtil(this);
            Boolean hayInternet = utilidades_red.isConnectingToInternet();
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            if (hayInternet){
                //Aqui es donde tenemos que poner el codigo que queremos ejecutar ahora solo el ping despues el conectar
                backGround proceso = new backGround();
                proceso.execute();
            }else{
                Toast.makeText(this, "No hay Conexión a Internet", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Para que la conexion se pueda realizar se debe de hacer en segundo plano
     * asi que se usa AsyncTask para poder lanzar un hilo aparte
     * pero los demas metodos de la clase de conexión se crean de la misma manera
     * Los metodos se ejecutan en el orden que estan colocados
     * ---------------------------------------------------------------------------------------------
     */
    private class backGround extends AsyncTask<Void, Integer, String> {

        @Override
        protected void onPreExecute() {
            //mResultView.setText("onPreExecute realizado!");
            showProgress(true); // esto es para mostrar la pantalla de progresbar
        }

        @Override
        protected String doInBackground(Void... Void) {
            String resultExec = null;
            try {
                conexionPorSSH conexion = new conexionPorSSH(_activity, mIPServer, mIPServerPort, mUser, mPassword, 3*1000);
                conexion.conectar();
                if (conexion.sessionConectado()){
                    resultExec = conexion.ejecutar("ls");
                }
            }catch (JSchException e){
                resultExec= e.getMessage();
            } catch (IOException e) {
                resultExec= e.getMessage();
            }
            return resultExec;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int progreso = values[0].intValue();
        }

        @Override
        protected void onPostExecute(String result) {
            showProgress(false);
            mResultView.setText(result);
        }

        @Override
        protected void onCancelled() {
            mResultView.setText("onCancelled realizado!");
        }
        // Hasta aqui llegan los metodos de uso de AsyncTask--------------------------------------------
    }

    /**
     * Metodo para mostrar una barra de progreso
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = this.getResources().getInteger(android.R.integer.config_shortAnimTime);
            this.findViewById(R.id.login_status).setVisibility(View.VISIBLE);
            this.findViewById(R.id.login_status).animate()
                    //mLoginStatusView.setVisibility(View.VISIBLE);
                    //mLoginStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            findViewById(R.id.login_status).setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            this.findViewById(R.id.login_form).setVisibility(View.VISIBLE);
            this.findViewById(R.id.login_form).animate()
                    //mLoginFormView.setVisibility(View.VISIBLE);
                    //mLoginFormView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            findViewById(R.id.login_form).setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            this.findViewById(R.id.login_status).setVisibility(show ? View.VISIBLE : View.GONE);
            this.findViewById(R.id.login_form).setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
