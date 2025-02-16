package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Data.User;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Remote.FirebaseAuthManager;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Database.Remote.FirestoreManager;
import edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.Persistance.AppPersistance;

//Actividad encargada de gestionar el proceso de inicio de sesión con Google mediante Firebase.
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;

    //ActivityResultLauncher se encarga de manejar el resultado del intento de inicio de sesión.
    private final ActivityResultLauncher<IntentSenderRequest> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        // Obtiene las credenciales devueltas por Google
                        SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                        String idToken = credential.getGoogleIdToken();
                        if (idToken != null) {
                            // Inicia sesión en Firebase con el token recibido
                            firebaseAuthWithGoogle(idToken);
                        }
                    } catch (ApiException e) {
                        Log.e(TAG, "Error al obtener las credenciales de Google", e);
                    }
                } else {
                    Log.w(TAG, "Inicio de sesión cancelado o fallido\n\n"+result.getResultCode()+"\n"+result.getData().toString()+"\n");
                }
            });

    /* Método que se ejecuta al crear la actividad. Inicializa Firebase, configura One Tap Sign-In y
    establece la acción del botón de inicio de sesión.*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializa Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configura el cliente de inicio de sesión One Tap de Google
        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                .setSupported(true)
                                .setServerClientId(getString(R.string.default_web_client_id))
                                .setFilterByAuthorizedAccounts(false)
                                .build())
                .build();

        EditText editTextEmail=findViewById(R.id.eTxtEmail);

        Button btnRegisterEmail=findViewById(R.id.btnRegisterEmail);
        btnRegisterEmail.setOnClickListener(v->{
            EditText eTxtEmail=findViewById(R.id.eTxtEmail);
            String email= eTxtEmail.getText().toString();
            EditText eTxtPassword=findViewById(R.id.eTxtPassword);
            String password= eTxtPassword.getText().toString();
            FirebaseAuthManager.register(this, email, password);
        });

        Button btnLoginEmail=findViewById(R.id.btnLoginEmail);
        btnLoginEmail.setOnClickListener(v -> {
            EditText eTxtEmail = findViewById(R.id.eTxtEmail);
            String email = eTxtEmail.getText().toString();
            EditText eTxtPassword = findViewById(R.id.eTxtPassword);
            String password = eTxtPassword.getText().toString();

            FirebaseAuthManager.login(this, email, password, new FirebaseAuthManager.OnLoginListener() {
                @Override
                public void onSuccess() {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        updateUI(user);
                    } else {
                        Toast.makeText(getApplicationContext(), "Error: No se pudo obtener el usuario", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(getApplicationContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });




        LoginButton fbLogingBnt= findViewById(R.id.login_button);
        fbLogingBnt.setPermissions("email","public_profile");
        CallbackManager callbackManager= CallbackManager.Factory.create() ;
        fbLogingBnt.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                setContentView(R.layout.activity_launcher);
                FirebaseAuth.getInstance().signInWithCredential(FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken()))
                        .addOnCompleteListener(LoginActivity.this,task -> {
                            if(task.isSuccessful()){
                                FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                                updateUI( user);
                                // Log.d("Facebook_Login",user.getEmail());
                            }
                        });
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(@NonNull FacebookException e) {

            }
        });


        editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    editTextEmail.setError("Correo electrónico inválido");
                } else {
                    editTextEmail.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });







        // Asigna el botón de inicio de sesión y define su comportamiento
        Button signInButton = findViewById(R.id.signInButton);
        signInButton.setOnClickListener(v -> startSignIn());
    }

    //Inicia el proceso de autenticación con Google mediante One Tap Sign-In.
    private void startSignIn() {
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, result -> {
                    try {
                        // Obtiene la solicitud de IntentSender para iniciar el flujo de inicio de sesión
                        IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();
                        signInLauncher.launch(intentSenderRequest);
                    } catch (Exception e) {
                        Log.e(TAG, "Error al iniciar el IntentSender", e);
                    }
                })
                .addOnFailureListener(this, e -> Log.e(TAG, "One Tap Sign-In Error: " + e.getMessage()));
    }

    //Autentica al usuario en Firebase con el token de Google obtenido.
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Obtiene el usuario autenticado
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d(TAG, "signInWithCredential:success, User: " + user.getDisplayName());
                        updateUI(user);
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        updateUI(null);
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    //Actualiza la interfaz de usuario dependiendo de si el usuario está autenticado o no.
    private void updateUI(FirebaseUser user) {
        if (user != null) {
                FirestoreManager.getUser(user.getEmail(), resultUser ->{
                    if (resultUser != null) {
                        AppPersistance.user = resultUser;
                        Log.d("AppPersis",AppPersistance.user.getEmail().toString());
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        finishAffinity();
                    }
                    else {
                        FirestoreManager.createUser(result ->{
                            if (result ) {
                                if (user  != null) {
                                    FirestoreManager.getUser(user.getEmail(),resultNewUser ->{
                                        if (resultNewUser != null) {
                                            AppPersistance.user = resultNewUser;
                                            Log.d("AppPersis",AppPersistance.user.getEmail().toString());
                                            Intent intent = new Intent(this, MainActivity.class);
                                            startActivity(intent);
                                            finishAffinity();
                                        } else {
                                            Log.e("FirestoreError", "No se pudo obtener el usuario desde Firestore");
                                        }
                                    });
                                }
                            } else {
                                Log.d("CREATE_USER","error al crear un nuevo usuario");
                            }
                        });
                        }
                });

        } else {
            Log.d(TAG, "Usuario no autenticado");
        }
    }
}
